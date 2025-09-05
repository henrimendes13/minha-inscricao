import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { LeaderboardResponse, Categoria } from '../../models/leaderboard.model';

@Injectable({
  providedIn: 'root'
})
export class LeaderboardService extends BaseHttpService {

  /**
   * Busca leaderboard de um evento por categoria
   */
  buscarLeaderboardPorEventoECategoria(eventoId: number, categoriaId: number): Observable<LeaderboardResponse> {
    return this.get<LeaderboardResponse>(API_CONFIG.endpoints.leaderboard.byEventoAndCategoria(eventoId, categoriaId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar leaderboard do evento ${eventoId}, categoria ${categoriaId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca categorias de um evento
   */
  buscarCategoriasPorEvento(eventoId: number): Observable<Categoria[]> {
    return this.get<Categoria[]>(API_CONFIG.endpoints.categorias.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar categorias do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }
}