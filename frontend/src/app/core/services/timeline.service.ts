import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { TimelineResponse } from '../../models/timeline.model';

@Injectable({
  providedIn: 'root'
})
export class TimelineService extends BaseHttpService {

  /**
   * Busca timeline de um evento específico
   */
  buscarTimelinePorEvento(eventoId: number): Observable<TimelineResponse> {
    return this.get<TimelineResponse>(API_CONFIG.endpoints.timeline.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar timeline do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }
}