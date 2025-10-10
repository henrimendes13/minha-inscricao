import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { Timeline, TimelineCreateRequest, TimelineUpdateRequest } from '../../models/timeline.model';

@Injectable({
  providedIn: 'root'
})
export class TimelineService extends BaseHttpService {

  /**
   * Busca timeline de um evento específico
   */
  buscarTimelinePorEvento(eventoId: number): Observable<Timeline> {
    return this.get<Timeline>(API_CONFIG.endpoints.timeline.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar timeline do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Verifica se timeline existe para um evento
   */
  verificarExistencia(eventoId: number): Observable<boolean> {
    return this.get<boolean>(`${API_CONFIG.endpoints.timeline.byEvento(eventoId)}/exists`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao verificar existência de timeline do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Cria nova timeline para um evento
   */
  criarTimeline(eventoId: number, timelineData: TimelineCreateRequest): Observable<Timeline> {
    return this.post<Timeline>(API_CONFIG.endpoints.timeline.byEvento(eventoId), timelineData)
      .pipe(
        catchError(error => {
          console.error(`Erro ao criar timeline para evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Atualiza timeline existente de um evento
   */
  atualizarTimeline(eventoId: number, timelineData: TimelineUpdateRequest): Observable<Timeline> {
    return this.put<Timeline>(API_CONFIG.endpoints.timeline.byEvento(eventoId), timelineData)
      .pipe(
        catchError(error => {
          console.error(`Erro ao atualizar timeline do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Deleta timeline de um evento
   */
  deletarTimeline(eventoId: number): Observable<void> {
    return this.delete<void>(API_CONFIG.endpoints.timeline.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao deletar timeline do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }
}
