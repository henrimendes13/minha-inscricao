import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

import { API_CONFIG } from '../constants/api.constants';
import { AnexoResponse } from '../../models/anexo.model';

@Injectable({
  providedIn: 'root'
})
export class AnexoService {

  constructor(private http: HttpClient) {
  }

  /**
   * Busca anexos/documentos de um evento específico
   */
  buscarAnexosPorEvento(eventoId: number): Observable<AnexoResponse> {
    return this.http.get<AnexoResponse>(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.anexos.base}/evento/${eventoId}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar anexos do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Download de um anexo usando HttpClient diretamente para evitar problemas com Blob
   */
  downloadAnexo(anexoId: number): Observable<Blob> {
    const url = `${API_CONFIG.baseUrl}${API_CONFIG.endpoints.anexos.base}/${anexoId}/download`;
    return this.http.get(url, { 
      responseType: 'blob',
      observe: 'body'
    })
      .pipe(
        catchError(error => {
          console.error(`Erro ao fazer download do anexo ${anexoId}:`, error);
          return throwError(() => error);
        })
      );
  }
}