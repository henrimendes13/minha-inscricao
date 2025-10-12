import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
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
}
