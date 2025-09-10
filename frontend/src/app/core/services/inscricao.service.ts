import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';

export interface ParticipanteDTO {
  id: number;
  nome: string;
  nomeEquipe?: string;
  genero?: string;
  statusInscricao?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InscricaoService extends BaseHttpService {

  /**
   * Busca participantes (atletas/equipes) inscritos e confirmados em uma categoria
   */
  getParticipantesByCategoria(eventoId: number, categoriaId: number): Observable<ParticipanteDTO[]> {
    return this.get<ParticipanteDTO[]>(`/atletas/evento/${eventoId}/categoria/${categoriaId}`);
  }

  /**
   * Busca todas as inscrições de uma categoria
   */
  getInscricoesByCategoria(categoriaId: number): Observable<any[]> {
    return this.get<any[]>(`${API_CONFIG.endpoints.inscricoes.base}/categoria/${categoriaId}`);
  }

  /**
   * Busca todas as inscrições de um evento
   */
  getInscricoesByEvento(eventoId: number): Observable<any[]> {
    return this.get<any[]>(API_CONFIG.endpoints.inscricoes.byEvento(eventoId));
  }
}