import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';

export interface UsuarioSummary {
  id: number;
  nome: string;
  email: string;
  tipo: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService extends BaseHttpService {
  /**
   * Busca todos os usuários
   */
  buscarTodos(): Observable<UsuarioSummary[]> {
    return this.get<UsuarioSummary[]>(API_CONFIG.endpoints.usuarios.base);
  }

  /**
   * Busca usuário por ID
   */
  buscarPorId(id: number): Observable<any> {
    return this.get<any>(`${API_CONFIG.endpoints.usuarios.base}/${id}`);
  }

  /**
   * Busca usuário por email
   */
  buscarPorEmail(email: string): Observable<UsuarioSummary | null> {
    return this.get<UsuarioSummary>(`${API_CONFIG.endpoints.usuarios.base}/email/${email}`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Verifica se existe um usuário com o email fornecido
   */
  verificarEmail(email: string): Observable<{exists: boolean, usuario?: {id: number, nome: string, email: string}}> {
    return this.get<{exists: boolean, usuario?: {id: number, nome: string, email: string}}>(
      `${API_CONFIG.endpoints.usuarios.base}/verificar-email?email=${encodeURIComponent(email)}`
    );
  }
}
