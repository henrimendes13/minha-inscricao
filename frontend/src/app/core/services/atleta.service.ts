import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';

export interface AtletaSummary {
  id: number;
  nome: string;
  cpf: string;
  email?: string;
  genero: string;
}

@Injectable({
  providedIn: 'root'
})
export class AtletaService extends BaseHttpService {
  /**
   * Busca todos os atletas
   */
  buscarTodos(): Observable<AtletaSummary[]> {
    return this.get<AtletaSummary[]>(API_CONFIG.endpoints.atletas.base);
  }

  /**
   * Busca atleta por ID
   */
  buscarPorId(id: number): Observable<any> {
    return this.get<any>(`${API_CONFIG.endpoints.atletas.base}/${id}`);
  }

  /**
   * Busca atletas por evento
   */
  buscarPorEvento(eventoId: number): Observable<AtletaSummary[]> {
    return this.get<AtletaSummary[]>(`${API_CONFIG.endpoints.atletas.base}/evento/${eventoId}`);
  }

  /**
   * Busca atletas por nome ou CPF
   */
  buscarPorNomeOuCpf(termo: string): Observable<AtletaSummary[]> {
    return this.get<AtletaSummary[]>(
      `${API_CONFIG.endpoints.atletas.base}/buscar-por-termo?termo=${encodeURIComponent(termo)}`
    );
  }
}
