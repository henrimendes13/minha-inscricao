import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { Workout } from '../../models/workout.model';

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
}
