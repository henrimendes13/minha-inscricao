import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { CategoriaInscricao } from '../../models/leaderboard.model';
import {
  CategoriaCreateRequest,
  CategoriaUpdateRequest,
  CategoriaApiResponse,
  CategoriaSummaryResponse
} from '../../models/categoria.model';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService extends BaseHttpService {

  /**
   * Cria uma nova categoria para um evento
   */
  criarCategoria(eventoId: number, data: CategoriaCreateRequest): Observable<CategoriaApiResponse> {
    return this.post<CategoriaApiResponse>(API_CONFIG.endpoints.categorias.create(eventoId), data)
      .pipe(
        catchError(error => {
          console.error(`Erro ao criar categoria para evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Atualiza uma categoria existente
   */
  atualizarCategoria(id: number, data: CategoriaUpdateRequest): Observable<CategoriaApiResponse> {
    return this.put<CategoriaApiResponse>(API_CONFIG.endpoints.categorias.update(id), data)
      .pipe(
        catchError(error => {
          console.error(`Erro ao atualizar categoria ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Deleta uma categoria
   */
  deletarCategoria(id: number): Observable<any> {
    return this.delete<any>(API_CONFIG.endpoints.categorias.delete(id))
      .pipe(
        catchError(error => {
          console.error(`Erro ao deletar categoria ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Lista todas as categorias de um evento (para gerenciamento)
   */
  listarCategoriasPorEvento(eventoId: number): Observable<CategoriaSummaryResponse[]> {
    return this.get<CategoriaSummaryResponse[]>(API_CONFIG.endpoints.categorias.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao listar categorias do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca uma categoria completa por ID
   */
  buscarCategoriaPorIdCompleta(id: number): Observable<CategoriaApiResponse> {
    return this.get<CategoriaApiResponse>(API_CONFIG.endpoints.categorias.byId(id))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar categoria ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Ativa uma categoria
   */
  ativarCategoria(id: number): Observable<CategoriaApiResponse> {
    return this.patch<CategoriaApiResponse>(API_CONFIG.endpoints.categorias.ativar(id), {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao ativar categoria ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Desativa uma categoria
   */
  desativarCategoria(id: number): Observable<CategoriaApiResponse> {
    return this.patch<CategoriaApiResponse>(API_CONFIG.endpoints.categorias.desativar(id), {})
      .pipe(
        catchError(error => {
          console.error(`Erro ao desativar categoria ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  // ==================== MÉTODOS LEGADOS (para compatibilidade com código existente) ====================

  /**
   * Busca categorias de um evento específico para inscrição
   */
  buscarCategoriasPorEvento(eventoId: number): Observable<CategoriaInscricao[]> {
    return this.get<CategoriaInscricao[]>(API_CONFIG.endpoints.categorias.byEvento(eventoId))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar categorias do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Busca categoria por ID (versão legada)
   */
  buscarCategoriaPorId(id: number): Observable<CategoriaInscricao> {
    return this.get<CategoriaInscricao>(API_CONFIG.endpoints.categorias.byId(id))
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar categoria ${id}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Filtra categorias ativas apenas
   */
  filtrarCategoriasAtivas(categorias: CategoriaInscricao[]): CategoriaInscricao[] {
    return categorias.filter(categoria => categoria.ativa);
  }

  /**
   * Formata o valor da inscrição para exibição
   */
  formatarValor(valor: number): string {
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  }

  /**
   * Verifica se uma categoria aceita inscrições
   */
  podeInscrever(categoria: CategoriaInscricao): boolean {
    return categoria.ativa && categoria.valorInscricao >= 0;
  }

  /**
   * Calcula o valor total de uma seleção de categorias
   */
  calcularValorTotal(categorias: CategoriaInscricao[], quantidades: Map<number, number>): number {
    let total = 0;

    categorias.forEach(categoria => {
      const quantidade = quantidades.get(categoria.id) || 0;
      total += categoria.valorInscricao * quantidade;
    });

    return total;
  }
}