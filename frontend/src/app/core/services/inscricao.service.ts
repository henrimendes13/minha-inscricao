import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import {
  InscricaoCreateRequest,
  InscricaoUpdateRequest,
  InscricaoSummaryResponse,
  InscricaoDetailedResponse,
  StatusInscricao
} from '../../models/inscricao.model';

@Injectable({
  providedIn: 'root'
})
export class InscricaoService extends BaseHttpService {

  /**
   * Busca todas as inscrições
   */
  buscarTodas(): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(API_CONFIG.endpoints.inscricoes.base)
      .pipe(
        catchError(error => {
          console.error('Erro ao buscar todas as inscrições:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrição por ID
   */
  buscarPorId(id: number): Observable<InscricaoDetailedResponse> {
    return this.get<InscricaoDetailedResponse>(`${API_CONFIG.endpoints.inscricoes.base}/${id}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar inscrição ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Cria nova inscrição (manual pelo admin)
   */
  criar(inscricao: InscricaoCreateRequest): Observable<InscricaoDetailedResponse> {
    return this.post<InscricaoDetailedResponse>(API_CONFIG.endpoints.inscricoes.base, inscricao)
      .pipe(
        catchError(error => {
          console.error('Erro ao criar inscrição:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Atualiza inscrição
   */
  atualizar(id: number, inscricao: InscricaoUpdateRequest): Observable<InscricaoDetailedResponse> {
    return this.put<InscricaoDetailedResponse>(`${API_CONFIG.endpoints.inscricoes.base}/${id}`, inscricao)
      .pipe(
        catchError(error => {
          console.error(`Erro ao atualizar inscrição ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Deleta inscrição
   */
  deletar(id: number): Observable<void> {
    return this.delete<void>(`${API_CONFIG.endpoints.inscricoes.base}/${id}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao deletar inscrição ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrições por evento
   */
  buscarPorEvento(eventoId: number): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(API_CONFIG.endpoints.inscricoes.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar inscrições do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrições por categoria
   */
  buscarPorCategoria(categoriaId: number): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(`${API_CONFIG.endpoints.inscricoes.base}/categoria/${categoriaId}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar inscrições da categoria ${categoriaId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrições por equipe
   */
  buscarPorEquipe(equipeId: number): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(`${API_CONFIG.endpoints.inscricoes.base}/equipe/${equipeId}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar inscrições da equipe ${equipeId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrições por status
   */
  buscarPorStatus(status: StatusInscricao): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(`${API_CONFIG.endpoints.inscricoes.base}/status/${status}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar inscrições com status ${status}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrições confirmadas
   */
  buscarConfirmadas(): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(`${API_CONFIG.endpoints.inscricoes.base}/confirmadas`)
      .pipe(
        catchError(error => {
          console.error('Erro ao buscar inscrições confirmadas:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrições pendentes
   */
  buscarPendentes(): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(`${API_CONFIG.endpoints.inscricoes.base}/pendentes`)
      .pipe(
        catchError(error => {
          console.error('Erro ao buscar inscrições pendentes:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca inscrições canceladas
   */
  buscarCanceladas(): Observable<InscricaoSummaryResponse[]> {
    return this.get<InscricaoSummaryResponse[]>(`${API_CONFIG.endpoints.inscricoes.base}/canceladas`)
      .pipe(
        catchError(error => {
          console.error('Erro ao buscar inscrições canceladas:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Confirma uma inscrição
   */
  confirmar(id: number): Observable<InscricaoDetailedResponse> {
    return this.patch<InscricaoDetailedResponse>(`${API_CONFIG.endpoints.inscricoes.base}/${id}/confirmar`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao confirmar inscrição ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Cancela uma inscrição
   */
  cancelar(id: number, motivo: string): Observable<InscricaoDetailedResponse> {
    return this.patch<InscricaoDetailedResponse>(`${API_CONFIG.endpoints.inscricoes.base}/${id}/cancelar`, { motivo })
      .pipe(
        catchError(error => {
          console.error(`Erro ao cancelar inscrição ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Coloca inscrição em lista de espera
   */
  colocarEmListaEspera(id: number): Observable<InscricaoDetailedResponse> {
    return this.patch<InscricaoDetailedResponse>(`${API_CONFIG.endpoints.inscricoes.base}/${id}/lista-espera`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao colocar inscrição ${id} em lista de espera:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Conta inscrições por evento e status
   */
  contarPorEventoEStatus(eventoId: number, status: StatusInscricao): Observable<{ count: number }> {
    return this.get<{ count: number }>(`${API_CONFIG.endpoints.inscricoes.base}/evento/${eventoId}/status/${status}/count`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao contar inscrições do evento ${eventoId} com status ${status}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Conta inscrições por categoria e status
   */
  contarPorCategoriaEStatus(categoriaId: number, status: StatusInscricao): Observable<{ count: number }> {
    return this.get<{ count: number }>(`${API_CONFIG.endpoints.inscricoes.base}/categoria/${categoriaId}/status/${status}/count`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao contar inscrições da categoria ${categoriaId} com status ${status}:`, error);
          return throwError(() => error);
        })
      );
  }

  // ==================== LEGACY METHODS ====================

  validarCpf(cpf: string): boolean {
    return !!(cpf && cpf.length >= 11);
  }

  validarTelefone(telefone: string): boolean {
    return !!(telefone && telefone.length >= 10);
  }

  formatarCpf(cpf: string): string {
    const numeros = cpf.replace(/\D/g, '');
    return numeros.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  formatarTelefone(telefone: string): string {
    const numeros = telefone.replace(/\D/g, '');
    if (numeros.length === 11) {
      return numeros.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }
    return numeros.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  }

  criarInscricaoIndividual(eventoId: number, atletaData: any): Observable<any> {
    console.warn('Método criarInscricaoIndividual está deprecated. Use criar() com InscricaoCreateRequest');
    return throwError(() => new Error('Método não implementado. Use criar() com InscricaoCreateRequest'));
  }

  criarInscricaoEquipe(eventoId: number, equipeData: any): Observable<any> {
    console.warn('Método criarInscricaoEquipe está deprecated. Use criar() com InscricaoCreateRequest');
    return throwError(() => new Error('Método não implementado. Use criar() com InscricaoCreateRequest'));
  }

  getParticipantesByCategoria(eventoId: number, categoriaId: number): Observable<ParticipanteDTO[]> {
    console.warn('Método getParticipantesByCategoria está deprecated');
    return throwError(() => new Error('Método não implementado'));
  }
}

export interface ParticipanteDTO {
  id: number;
  nome: string;
  tipo: string;
  nomeEquipe?: string;
}
 
