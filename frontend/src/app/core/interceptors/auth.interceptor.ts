import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthService } from '../auth/auth.service';
import { isTokenExpired } from '../auth/auth-utils';

/**
 * Interceptor de autenticação melhorado
 * 
 * Funcionalidades:
 * - Adiciona token JWT automaticamente nas requisições
 * - Trata erros 401/403 de forma inteligente
 * - Mostra mensagens de erro amigáveis ao usuário
 * - Faz logout automático quando necessário
 * - Tenta renovar tokens expirados (quando suportado pelo backend)
 * 
 * @example
 * // O interceptor funciona automaticamente, mas você pode verificar logs no console:
 * // [AUTH-INTERCEPTOR] Token expirado - fazendo logout
 * // [AUTH-INTERCEPTOR] Erro de rede - servidor pode estar offline
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let authReq = req;
    const token = this.authService.getToken();

    if (token) {
      authReq = this.addToken(req, token);
    }

    return next.handle(authReq).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse) {
          return this.handleHttpError(error, authReq, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addToken(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  /**
   * Trata erros HTTP de forma centralizada
   */
  private handleHttpError(error: HttpErrorResponse, request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    console.error('[AUTH-INTERCEPTOR] Erro HTTP:', {
      status: error.status,
      message: error.message,
      url: error.url
    });

    switch (error.status) {
      case 401:
        return this.handle401Unauthorized(request, next, error);
      case 403:
        return this.handle403Forbidden(error);
      case 0:
        return this.handleNetworkError(error);
      default:
        return this.handleGenericError(error);
    }
  }

  /**
   * Trata erro 401 - Token inválido/expirado
   */
  private handle401Unauthorized(request: HttpRequest<any>, next: HttpHandler, error: HttpErrorResponse): Observable<HttpEvent<any>> {
    // Se é endpoint de autenticação ou logout, não tenta refresh
    if (this.isAuthEndpoint(request.url) || this.isLogoutEndpoint(request.url)) {
      return throwError(() => error);
    }

    const token = this.authService.getToken();
    
    // Se não há token ou está claramente expirado, faz logout
    if (!token || isTokenExpired(token)) {
      console.warn('[AUTH-INTERCEPTOR] Token expirado ou inválido - fazendo logout');
      this.handleTokenExpired();
      return throwError(() => error);
    }

    // Tenta refresh token (embora o backend não suporte)
    return this.attemptTokenRefresh(request, next, error);
  }

  /**
   * Trata erro 403 - Acesso negado
   */
  private handle403Forbidden(error: HttpErrorResponse): Observable<HttpEvent<any>> {
    console.warn('[AUTH-INTERCEPTOR] Acesso negado (403):', error.url);
    
    // Se é endpoint de logout, não mostrar erro para o usuário
    if (this.isLogoutEndpoint(error.url)) {
      return throwError(() => error);
    }
    
    this.snackBar.open('Acesso negado - Você não tem permissão para esta ação', 'Fechar', {
      duration: 5000,
      panelClass: ['error-snackbar'],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });

    return throwError(() => error);
  }

  /**
   * Trata erro de rede (status 0)
   */
  private handleNetworkError(error: HttpErrorResponse): Observable<HttpEvent<any>> {
    console.error('[AUTH-INTERCEPTOR] Erro de rede - servidor pode estar offline');
    
    this.snackBar.open('Erro de conexão - Verifique sua internet e tente novamente', 'Fechar', {
      duration: 7000,
      panelClass: ['error-snackbar'],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });

    return throwError(() => error);
  }

  /**
   * Trata outros erros HTTP
   */
  private handleGenericError(error: HttpErrorResponse): Observable<HttpEvent<any>> {
    let message = 'Erro no servidor';
    
    if (error.status >= 500) {
      message = 'Erro interno do servidor - Tente novamente mais tarde';
    } else if (error.status >= 400) {
      message = error.error?.message || 'Erro na solicitação';
    }

    this.snackBar.open(message, 'Fechar', {
      duration: 5000,
      panelClass: ['error-snackbar'],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });

    return throwError(() => error);
  }

  /**
   * Tenta renovar o token (embora o backend atual não suporte)
   */
  private attemptTokenRefresh(request: HttpRequest<any>, next: HttpHandler, originalError: HttpErrorResponse): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);


      return this.authService.refreshToken().pipe(
        switchMap((token: any) => {
          this.isRefreshing = false;
          this.refreshTokenSubject.next(token.accessToken);
          return next.handle(this.addToken(request, token.accessToken));
        }),
        catchError((refreshError) => {
          console.warn('[AUTH-INTERCEPTOR] Falha ao renovar token:', refreshError);
          this.isRefreshing = false;
          this.handleTokenExpired();
          return throwError(() => originalError);
        })
      );
    } else {
      // Aguarda o refresh em andamento
      return this.refreshTokenSubject.pipe(
        filter(token => token != null),
        take(1),
        switchMap(jwt => {
          return next.handle(this.addToken(request, jwt));
        })
      );
    }
  }

  /**
   * Trata token expirado fazendo logout e redirecionando
   */
  private handleTokenExpired(): void {
    
    this.snackBar.open('Sua sessão expirou - Faça login novamente', 'Fechar', {
      duration: 5000,
      panelClass: ['warning-snackbar'],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });

    // Pequeno delay para mostrar a mensagem antes do redirect
    setTimeout(() => {
      this.authService.logout();
      this.router.navigate(['/login'], { 
        queryParams: { 
          returnUrl: this.router.url,
          reason: 'session-expired' 
        }
      });
    }, 1000);
  }

  /**
   * Verifica se a URL é de um endpoint de logout
   * Força recompilação - change
   */
  private isLogoutEndpoint(url: string | null): boolean {
    if (!url) return false;
    return url.includes('/logout') || url.includes('/auth/logout');
  }

  /**
   * Verifica se a URL é de um endpoint de autenticação
   */
  private isAuthEndpoint(url: string | null): boolean {
    if (!url) return false;
    return url.includes('/login') || url.includes('/auth/login') || url.includes('/register') || url.includes('/auth/register');
  }
}
