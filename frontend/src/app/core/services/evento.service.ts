import { Injectable } from '@angular/core';
import { Observable, map, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { EventoApiResponse, EventoCreateRequest, EventoUpdateRequest } from '../../models/evento.model';

@Injectable({
  providedIn: 'root'
})
export class EventoService extends BaseHttpService {

  /**
   * Lista todos os eventos
   */
  listarEventos(): Observable<EventoApiResponse[]> {
    return this.get<EventoApiResponse[]>(API_CONFIG.endpoints.eventos.base)
      .pipe(
        map(eventos => eventos.sort((a, b) => 
          new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        )),
        catchError(error => {
          console.error('Erro ao buscar eventos:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca evento por ID
   */
  buscarEventoPorId(id: number): Observable<EventoApiResponse> {
    return this.get<EventoApiResponse>(API_CONFIG.endpoints.eventos.byId(id))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca eventos por status
   */
  buscarEventosPorStatus(status: string): Observable<EventoApiResponse[]> {
    return this.listarEventos().pipe(
      map(eventos => eventos.filter(evento => evento.status === status))
    );
  }

  /**
   * Formata data para exibição
   */
  formatarData(dataIso: string): string {
    const data = new Date(dataIso);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  /**
   * Formata horário para exibição
   */
  formatarHorario(dataIso: string): string {
    const data = new Date(dataIso);
    return data.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Retorna cor do status para UI
   */
  getStatusColor(status: string): string {
    switch (status) {
      case 'ABERTO':
        return '#4caf50';
      case 'RASCUNHO':
        return '#ff9800';
      case 'FECHADO':
        return '#f44336';
      default:
        return '#607d8b';
    }
  }

  /**
   * Cria novo evento
   */
  criarEvento(evento: EventoCreateRequest): Observable<EventoApiResponse> {
    return this.post<EventoApiResponse>(API_CONFIG.endpoints.eventos.base, evento)
      .pipe(
        catchError(error => {
          console.error('Erro ao criar evento:', error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Atualiza evento existente
   */
  atualizarEvento(id: number, evento: EventoUpdateRequest): Observable<EventoApiResponse> {
    return this.put<EventoApiResponse>(API_CONFIG.endpoints.eventos.byId(id), evento)
      .pipe(
        catchError(error => {
          console.error(`Erro ao atualizar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Deleta evento
   */
  deletarEvento(id: number): Observable<void> {
    return this.delete<void>(API_CONFIG.endpoints.eventos.byId(id))
      .pipe(
        catchError(error => {
          console.error(`Erro ao deletar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Publica evento (muda status para ABERTO)
   */
  publicarEvento(id: number): Observable<EventoApiResponse> {
    return this.put<EventoApiResponse>(`${API_CONFIG.endpoints.eventos.byId(id)}/publicar`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao publicar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Encerra inscrições do evento
   */
  encerrarInscricoes(id: number): Observable<EventoApiResponse> {
    return this.put<EventoApiResponse>(`${API_CONFIG.endpoints.eventos.byId(id)}/encerrar-inscricoes`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao encerrar inscrições do evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Inicia evento (muda status para EM_ANDAMENTO)
   */
  iniciarEvento(id: number): Observable<EventoApiResponse> {
    return this.put<EventoApiResponse>(`${API_CONFIG.endpoints.eventos.byId(id)}/iniciar`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao iniciar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Finaliza evento (muda status para FINALIZADO)
   */
  finalizarEvento(id: number): Observable<EventoApiResponse> {
    return this.put<EventoApiResponse>(`${API_CONFIG.endpoints.eventos.byId(id)}/finalizar`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao finalizar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Cancela evento (muda status para CANCELADO)
   */
  cancelarEvento(id: number): Observable<EventoApiResponse> {
    return this.put<EventoApiResponse>(`${API_CONFIG.endpoints.eventos.byId(id)}/cancelar`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao cancelar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Adia evento (muda status para ADIADO)
   */
  adiarEvento(id: number): Observable<EventoApiResponse> {
    return this.put<EventoApiResponse>(`${API_CONFIG.endpoints.eventos.byId(id)}/adiar`, {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao adiar evento ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Converte data do DatePicker para formato do backend (dd-MM-yyyy)
   */
  converterDataParaBackend(data: Date): string {
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${dia}-${mes}-${ano}`;
  }

  /**
   * Converte data do backend (dd-MM-yyyy) para Date
   */
  converterDataDoBackend(dataStr: string): Date {
    const [dia, mes, ano] = dataStr.split('-').map(Number);
    return new Date(ano, mes - 1, dia);
  }

  /**
   * Verifica se evento está aberto para inscrições
   */
  isEventoAberto(evento: EventoApiResponse): boolean {
    return evento.status === 'ABERTO' && evento.podeReceberInscricoes;
  }
}
