import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, catchError, finalize } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

import { API_CONFIG } from '../constants/api.constants';
import { UsuarioResponseDTO } from '../../models';
import { isTokenExpired, debugToken } from './auth-utils';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  email: string;
  nome: string;
  userId: number;
  tipoUsuario: string;
  role: string;
  loginAt: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
  tipoUsuario: 'ORGANIZADOR' | 'ATLETA';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Estados observáveis
  private currentUserSubject = new BehaviorSubject<UsuarioResponseDTO | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  // Estados de loading
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();
  
  private isLoggingInSubject = new BehaviorSubject<boolean>(false);
  public isLoggingIn$ = this.isLoggingInSubject.asObservable();
  
  private isLoggingOutSubject = new BehaviorSubject<boolean>(false);
  public isLoggingOut$ = this.isLoggingOutSubject.asObservable();
  
  // Constantes
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  constructor(
    private http: HttpClient,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loadUserFromStorage();
  }

  /**
   * Realiza login do usuário
   * Emite estados de loading durante o processo
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    console.log('[AUTH-SERVICE] Iniciando login para:', credentials.email);
    
    this.setLoading(true);
    this.isLoggingInSubject.next(true);
    
    return this.http.post<LoginResponse>(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth.login}`, credentials)
      .pipe(
        map(response => {
          console.log('[AUTH-SERVICE] Login bem-sucedido');
          this.setSession(response);
          return response;
        }),
        catchError(error => {
          console.error('[AUTH-SERVICE] Erro no login:', error);
          return throwError(() => error);
        }),
        finalize(() => {
          this.setLoading(false);
          this.isLoggingInSubject.next(false);
        })
      );
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth.register}`, userData);
  }

  refreshToken(): Observable<{ accessToken: string }> {
    // Backend não fornece refresh token, então retornamos erro
    return throwError(() => new Error('Refresh token not supported by backend'));
  }

  /**
   * Realiza logout do usuário
   * Emite estados de loading durante o processo
   */
  logout(): void {
    console.log('[AUTH-SERVICE] Iniciando logout');
    
    this.setLoading(true);
    this.isLoggingOutSubject.next(true);
    
    // Call backend logout endpoint if needed
    this.http.post(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth.logout}`, {})
      .subscribe({
        next: () => {
          console.log('[AUTH-SERVICE] Logout do servidor bem-sucedido');
          this.clearSessionWithSuccess();
        },
        error: (error) => {
          console.warn('[AUTH-SERVICE] Erro no logout do servidor (continuando com logout local):', error);
          // Even if the request fails, clear local session
          this.clearSessionWithSuccess();
        },
        complete: () => {
          this.setLoading(false);
          this.isLoggingOutSubject.next(false);
        }
      });
  }

  getToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getCurrentUser(): UsuarioResponseDTO | null {
    return this.currentUserSubject.value;
  }

  /**
   * Verifica se o usuário está autenticado
   * Usa utilitários para verificação mais robusta
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    
    if (!token) {
      console.debug('[AUTH-SERVICE] Não há token - não autenticado');
      return false;
    }

    const expired = isTokenExpired(token);
    
    if (expired === null) {
      console.warn('[AUTH-SERVICE] Token inválido - não autenticado');
      return false;
    }
    
    if (expired === true) {
      console.warn('[AUTH-SERVICE] Token expirado - limpando sessão');
      this.clearSession();
      return false;
    }
    
    console.debug('[AUTH-SERVICE] Usuário autenticado');
    return true;
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.tipoUsuario === role;
  }

  private setSession(response: LoginResponse): void {
    console.log('[AUTH-SERVICE] Configurando sessão para:', response.email);
    
    try {
      localStorage.setItem(this.ACCESS_TOKEN_KEY, response.token);
      localStorage.setItem(this.REFRESH_TOKEN_KEY, ''); // Backend não fornece refresh token
      
      // Criar objeto usuário a partir dos dados da resposta
      const usuario = {
        id: response.userId,
        nome: response.nome,
        email: response.email,
        tipoUsuario: response.tipoUsuario,
        role: response.role
      };
      
      localStorage.setItem(this.USER_KEY, JSON.stringify(usuario));
      this.currentUserSubject.next(usuario as any);
      
      console.log('[AUTH-SERVICE] Sessão configurada com sucesso');
    } catch (error) {
      console.error('[AUTH-SERVICE] Erro ao configurar sessão:', error);
      throw error;
    }
  }

  private clearSession(): void {
    console.log('[AUTH-SERVICE] Limpando sessão');
    
    try {
      localStorage.removeItem(this.ACCESS_TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      this.currentUserSubject.next(null);
      
      console.log('[AUTH-SERVICE] Sessão limpa - redirecionando para login');
      this.router.navigate(['/login']);
    } catch (error) {
      console.error('[AUTH-SERVICE] Erro ao limpar sessão:', error);
    }
  }

  /**
   * Limpa a sessão e mostra mensagem de sucesso
   */
  private clearSessionWithSuccess(): void {
    console.log('[AUTH-SERVICE] Limpando sessão com mensagem de sucesso');
    
    try {
      localStorage.removeItem(this.ACCESS_TOKEN_KEY);
      localStorage.removeItem(this.REFRESH_TOKEN_KEY);
      localStorage.removeItem(this.USER_KEY);
      this.currentUserSubject.next(null);
      
      // Mostrar mensagem de sucesso
      this.snackBar.open('Logout realizado com sucesso!', 'Fechar', {
        duration: 3000,
        panelClass: ['success-snackbar'],
        horizontalPosition: 'center',
        verticalPosition: 'top'
      });
      
      console.log('[AUTH-SERVICE] Sessão limpa - redirecionando para login');
      this.router.navigate(['/login']);
    } catch (error) {
      console.error('[AUTH-SERVICE] Erro ao limpar sessão:', error);
    }
  }

  /**
   * Carrega usuário do localStorage na inicialização
   */
  private loadUserFromStorage(): void {
    console.log('[AUTH-SERVICE] Carregando usuário do localStorage');
    
    this.setLoading(true);
    
    try {
      const userStr = localStorage.getItem(this.USER_KEY);
      
      if (userStr && this.isAuthenticated()) {
        const user = JSON.parse(userStr);
        console.log('[AUTH-SERVICE] Usuário carregado:', user.email);
        this.currentUserSubject.next(user);
      } else {
        console.debug('[AUTH-SERVICE] Nenhum usuário válido no localStorage');
        this.currentUserSubject.next(null);
      }
    } catch (error) {
      console.error('[AUTH-SERVICE] Erro ao carregar usuário do localStorage:', error);
      this.clearSession();
    } finally {
      this.setLoading(false);
    }
  }
  
  /**
   * Define estado de loading geral
   */
  private setLoading(loading: boolean): void {
    this.isLoadingSubject.next(loading);
  }
  
  /**
   * Obtém estado de loading atual
   */
  public isLoading(): boolean {
    return this.isLoadingSubject.value;
  }
  
  /**
   * Obtém estado de login em andamento
   */
  public isLoggingIn(): boolean {
    return this.isLoggingInSubject.value;
  }
  
  /**
   * Obtém estado de logout em andamento
   */
  public isLoggingOut(): boolean {
    return this.isLoggingOutSubject.value;
  }
  
  /**
   * Função utilitária para debug do estado de autenticação
   */
  public getAuthDebugInfo(): any {
    const token = this.getToken();
    return {
      isAuthenticated: this.isAuthenticated(),
      currentUser: this.getCurrentUser(),
      isLoading: this.isLoading(),
      isLoggingIn: this.isLoggingIn(),
      isLoggingOut: this.isLoggingOut(),
      tokenInfo: debugToken(token),
      timestamp: new Date().toISOString()
    };
  }
}