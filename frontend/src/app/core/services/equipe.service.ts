import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';

export interface EquipeSummary {
  id: number;
  nome: string;
  quantidadeAtletas: number;
  ativa: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class EquipeService extends BaseHttpService {
  /**
   * Busca todas as equipes
   */
  buscarTodas(): Observable<EquipeSummary[]> {
    return this.get<EquipeSummary[]>(API_CONFIG.endpoints.equipes.base);
  }

  /**
   * Busca equipe por ID
   */
  buscarPorId(id: number): Observable<any> {
    return this.get<any>(`${API_CONFIG.endpoints.equipes.base}/${id}`);
  }

  /**
   * Busca equipes por evento
   */
  buscarPorEvento(eventoId: number): Observable<EquipeSummary[]> {
    return this.get<EquipeSummary[]>(`${API_CONFIG.endpoints.equipes.base}/evento/${eventoId}`);
  }
}
