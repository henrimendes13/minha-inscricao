import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { CategoriaInscricao } from '../../models/leaderboard.model';

@Injectable({
  providedIn: 'root'
})
export class CategoriaService extends BaseHttpService {

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
   * Busca categoria por ID
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