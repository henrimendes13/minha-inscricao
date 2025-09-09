import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

import { API_CONFIG } from '../constants/api.constants';
import { LeaderboardResponse, Categoria, LeaderboardRanking } from '../../models/leaderboard.model';

@Injectable({
  providedIn: 'root'
})
export class LeaderboardService {

  constructor(private http: HttpClient) { }

  /**
   * Busca leaderboard de um evento por categoria (método legado)
   */
  buscarLeaderboardPorEventoECategoria(eventoId: number, categoriaId: number): Observable<LeaderboardResponse> {
    return this.http.get<LeaderboardResponse>(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.leaderboard.byEventoAndCategoria(eventoId, categoriaId)}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar leaderboard do evento ${eventoId}, categoria ${categoriaId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca ranking completo de uma categoria (novo método para tabela CrossFit style)
   */
  getRankingCategoria(eventoId: number, categoriaId: number): Observable<LeaderboardRanking[]> {
    const url = `${API_CONFIG.baseUrl}/leaderboards/evento/${eventoId}/categoria/${categoriaId}/ranking`;
    return this.http.get<LeaderboardRanking[]>(url)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar ranking da categoria ${categoriaId} no evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca categorias de um evento
   */
  buscarCategoriasPorEvento(eventoId: number): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.categorias.byEvento(eventoId)}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar categorias do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }
}
