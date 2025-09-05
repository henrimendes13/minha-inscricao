import { Injectable } from '@angular/core';
import { Observable, map, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { EventoApiResponse } from '../../models/evento.model';

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
   * Verifica se evento está aberto para inscrições
   */
  isEventoAberto(evento: EventoApiResponse): boolean {
    return evento.status === 'ABERTO' && evento.podeReceberInscricoes;
  }
}