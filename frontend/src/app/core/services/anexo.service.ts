import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { AnexoResponse } from '../../models/anexo.model';

@Injectable({
  providedIn: 'root'
})
export class AnexoService extends BaseHttpService {

  /**
   * Busca anexos/documentos de um evento específico
   */
  buscarAnexosPorEvento(eventoId: number): Observable<AnexoResponse> {
    return this.get<AnexoResponse>(`${API_CONFIG.endpoints.anexos.base}?eventoId=${eventoId}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar anexos do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Download de um anexo
   */
  downloadAnexo(anexoId: number): Observable<Blob> {
    return this.get<Blob>(API_CONFIG.endpoints.anexos.byId(anexoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao fazer download do anexo ${anexoId}:`, error);
          return throwError(() => error);
        })
      );
  }
}