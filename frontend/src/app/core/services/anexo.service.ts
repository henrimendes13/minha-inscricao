import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

import { API_CONFIG } from '../constants/api.constants';
import { AnexoResponse, AnexoDetailed } from '../../models/anexo.model';

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
   * Upload de um anexo para um evento
   */
  uploadAnexo(arquivo: File, eventoId: number, descricao?: string): Observable<AnexoDetailed> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    formData.append('eventoId', eventoId.toString());
    if (descricao) {
      formData.append('descricao', descricao);
    }

    return this.http.post<AnexoDetailed>(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.anexos.base}/upload`, formData)
      .pipe(
        catchError(error => {
          console.error(`Erro ao fazer upload do anexo para evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Remove um anexo
   */
  removerAnexo(anexoId: number): Observable<void> {
    return this.http.delete<void>(`${API_CONFIG.baseUrl}${API_CONFIG.endpoints.anexos.base}/${anexoId}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao remover anexo ${anexoId}:`, error);
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
