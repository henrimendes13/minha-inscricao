import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

import { API_CONFIG } from '../constants/api.constants';
import { UsuarioResponseDTO } from '../../models';

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
  private currentUserSubject = new BehaviorSubject<UsuarioResponseDTO | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth.login}`, credentials)
      .pipe(
        map(response => {
          this.setSession(response);
          return response;
        }),
        catchError(error => throwError(() => error))
      );
  }

  register(userData: RegisterRequest): Observable<any> {
    return this.http.post(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth.register}`, userData);
  }

  refreshToken(): Observable<{ accessToken: string }> {
    // Backend não fornece refresh token, então retornamos erro
    return throwError(() => new Error('Refresh token not supported by backend'));
  }

  logout(): void {
    // Call backend logout endpoint if needed
    this.http.post(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.auth.logout}`, {})
      .subscribe(() => {
        this.clearSession();
      }, () => {
        // Even if the request fails, clear local session
        this.clearSession();
      });
  }

  getToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  getCurrentUser(): UsuarioResponseDTO | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) {
      return false;
    }

    // Check if token is expired
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000;
      return Date.now() < exp;
    } catch (error) {
      return false;
    }
  }

  hasRole(role: string): boolean {
    const user = this.getCurrentUser();
    return user?.tipoUsuario === role;
  }

  private setSession(response: LoginResponse): void {
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
  }

  private clearSession(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  private loadUserFromStorage(): void {
    const userStr = localStorage.getItem(this.USER_KEY);
    if (userStr && this.isAuthenticated()) {
      const user = JSON.parse(userStr);
      this.currentUserSubject.next(user);
    }
  }
}