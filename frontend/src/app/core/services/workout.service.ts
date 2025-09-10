import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { 
  Workout, 
  WorkoutResultCreateDTO, 
  WorkoutResultUpdateDTO, 
  WorkoutResultStatusDTO,
  LeaderboardSummaryDTO,
  LeaderboardResponseDTO
} from '../../models/workout.model';

@Injectable({
  providedIn: 'root'
})
export class WorkoutService extends BaseHttpService {

  /**
   * Busca workouts de um evento específico
   */
  buscarWorkoutsPorEvento(eventoId: number): Observable<Workout[]> {
    return this.get<Workout[]>(API_CONFIG.endpoints.workouts.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar workouts do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  // ===============================================
  // MÉTODOS PARA GERENCIAMENTO DE RESULTADOS
  // ===============================================

  /**
   * Busca todos os resultados de um workout em uma categoria
   */
  getResultadosWorkout(workoutId: number, categoriaId: number): Observable<LeaderboardSummaryDTO[]> {
    const url = `${API_CONFIG.endpoints.workouts.resultados.byWorkout(workoutId)}?categoriaId=${categoriaId}`;
    
    return this.get<LeaderboardSummaryDTO[]>(url)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar resultados do workout ${workoutId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Registra novo resultado para um participante específico
   */
  adicionarResultado(workoutId: number, resultado: WorkoutResultCreateDTO): Observable<LeaderboardResponseDTO> {
    return this.post<LeaderboardResponseDTO>(
      API_CONFIG.endpoints.workouts.resultados.create(workoutId), 
      resultado
    ).pipe(
      catchError(error => {
        console.error(`Erro ao adicionar resultado para workout ${workoutId}:`, error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Atualiza resultado de uma equipe específica
   */
  atualizarResultadoEquipe(
    workoutId: number, 
    equipeId: number, 
    resultado: WorkoutResultUpdateDTO
  ): Observable<LeaderboardResponseDTO> {
    return this.put<LeaderboardResponseDTO>(
      API_CONFIG.endpoints.workouts.resultados.updateEquipe(workoutId, equipeId),
      resultado
    ).pipe(
      catchError(error => {
        console.error(`Erro ao atualizar resultado da equipe ${equipeId} no workout ${workoutId}:`, error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Atualiza resultado de um atleta específico
   */
  atualizarResultadoAtleta(
    workoutId: number, 
    atletaId: number, 
    resultado: WorkoutResultUpdateDTO
  ): Observable<LeaderboardResponseDTO> {
    return this.put<LeaderboardResponseDTO>(
      API_CONFIG.endpoints.workouts.resultados.updateAtleta(workoutId, atletaId),
      resultado
    ).pipe(
      catchError(error => {
        console.error(`Erro ao atualizar resultado do atleta ${atletaId} no workout ${workoutId}:`, error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Remove resultado de uma equipe
   */
  removerResultadoEquipe(workoutId: number, equipeId: number): Observable<void> {
    return this.delete<void>(
      API_CONFIG.endpoints.workouts.resultados.deleteEquipe(workoutId, equipeId)
    ).pipe(
      catchError(error => {
        console.error(`Erro ao remover resultado da equipe ${equipeId} no workout ${workoutId}:`, error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Remove resultado de um atleta
   */
  removerResultadoAtleta(workoutId: number, atletaId: number): Observable<void> {
    return this.delete<void>(
      API_CONFIG.endpoints.workouts.resultados.deleteAtleta(workoutId, atletaId)
    ).pipe(
      catchError(error => {
        console.error(`Erro ao remover resultado do atleta ${atletaId} no workout ${workoutId}:`, error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Busca status e estatísticas de um workout
   */
  getStatusWorkout(workoutId: number, categoriaId: number): Observable<WorkoutResultStatusDTO> {
    const url = `${API_CONFIG.endpoints.workouts.resultados.status(workoutId)}?categoriaId=${categoriaId}`;
    
    return this.get<WorkoutResultStatusDTO>(url)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar status do workout ${workoutId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Verifica se um workout já tem resultados inicializados
   */
  verificarResultadosInicializados(workoutId: number, categoriaId: number): Observable<{ temResultados: boolean }> {
    const url = `${API_CONFIG.endpoints.workouts.resultados.verificarInicializados(workoutId)}?categoriaId=${categoriaId}`;
    
    return this.get<{ temResultados: boolean }>(url)
      .pipe(
        catchError(error => {
          console.error(`Erro ao verificar inicialização de resultados do workout ${workoutId}:`, error);
          return throwError(() => error);
        })
      );
  }
}
