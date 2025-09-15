import { Injectable } from '@angular/core';
import { Observable, catchError, throwError } from 'rxjs';

import { BaseHttpService } from './base-http.service';
import { API_CONFIG } from '../constants/api.constants';
import { AtletaInscricaoDTO, EquipeInscricaoDTO } from '../../models/inscricao.model';
import { AtletaResponseDTO } from '../../models/atleta.model';
import { EquipeResponseDTO } from '../../models/equipe.model';

export interface ParticipanteDTO {
  id: number;
  nome: string;
  nomeEquipe?: string;
  genero?: string;
  statusInscricao?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InscricaoService extends BaseHttpService {

  /**
   * Busca participantes (atletas/equipes) inscritos e confirmados em uma categoria
   */
  getParticipantesByCategoria(eventoId: number, categoriaId: number): Observable<ParticipanteDTO[]> {
    return this.get<ParticipanteDTO[]>(`/atletas/evento/${eventoId}/categoria/${categoriaId}`);
  }

  /**
   * Busca todas as inscrições de uma categoria
   */
  getInscricoesByCategoria(categoriaId: number): Observable<any[]> {
    return this.get<any[]>(`${API_CONFIG.endpoints.inscricoes.base}/categoria/${categoriaId}`);
  }

  /**
   * Busca todas as inscrições de um evento
   */
  getInscricoesByEvento(eventoId: number): Observable<any[]> {
    return this.get<any[]>(API_CONFIG.endpoints.inscricoes.byEvento(eventoId));
  }

  // ===============================================
  // NOVOS MÉTODOS PARA CRIAÇÃO DE INSCRIÇÕES
  // ===============================================

  /**
   * Cria inscrição individual (atleta)
   */
  criarInscricaoIndividual(eventoId: number, atletaData: AtletaInscricaoDTO): Observable<AtletaResponseDTO> {
    const endpoint = `/atletas/evento/${eventoId}/inscricao/atletas`;
    
    return this.post<AtletaResponseDTO>(endpoint, atletaData)
      .pipe(
        catchError(error => {
          console.error(`Erro ao criar inscrição individual para evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Cria inscrição de equipe
   */
  criarInscricaoEquipe(eventoId: number, equipeData: EquipeInscricaoDTO, usuarioId?: number): Observable<EquipeResponseDTO> {
    let endpoint = `/equipes/evento/${eventoId}/inscricao`;
    
    // Adicionar usuarioId como query param se fornecido
    if (usuarioId) {
      endpoint += `?usuarioLogadoId=${usuarioId}`;
    }
    
    return this.post<EquipeResponseDTO>(endpoint, equipeData)
      .pipe(
        catchError(error => {
          console.error(`Erro ao criar inscrição de equipe para evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Verifica se um CPF já está cadastrado
   */
  verificarCpfExistente(cpf: string): Observable<{exists: boolean}> {
    return this.get<{exists: boolean}>(`/atletas/cpf/${cpf}/exists`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao verificar CPF ${cpf}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Lista atletas de um evento
   */
  buscarAtletasPorEvento(eventoId: number): Observable<AtletaResponseDTO[]> {
    return this.get<AtletaResponseDTO[]>(`/atletas/evento/${eventoId}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar atletas do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  /**
   * Lista equipes de um evento
   */
  buscarEquipesPorEvento(eventoId: number): Observable<EquipeResponseDTO[]> {
    return this.get<EquipeResponseDTO[]>(`/equipes/evento/${eventoId}`)
      .pipe(
        catchError(error => {
          console.error(`Erro ao buscar equipes do evento ${eventoId}:`, error);
          return throwError(() => error);
        })
      );
  }

  // ===============================================
  // MÉTODOS DE VALIDAÇÃO E UTILITÁRIOS
  // ===============================================

  /**
   * Valida formato de CPF
   */
  validarCpf(cpf: string): boolean {
    const cpfRegex = /^\d{3}\.\d{3}\.\d{3}-\d{2}$/;
    return cpfRegex.test(cpf);
  }

  /**
   * Valida formato de telefone
   */
  validarTelefone(telefone: string): boolean {
    const telefoneRegex = /^\(\d{2}\)\s\d{4,5}-\d{4}$/;
    return telefoneRegex.test(telefone);
  }

  /**
   * Formata CPF
   */
  formatarCpf(cpf: string): string {
    const numeros = cpf.replace(/\D/g, '');
    return numeros.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  /**
   * Formata telefone
   */
  formatarTelefone(telefone: string): string {
    const numeros = telefone.replace(/\D/g, '');
    if (numeros.length === 10) {
      return numeros.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    } else if (numeros.length === 11) {
      return numeros.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }
    return telefone;
  }

  /**
   * Calcula idade baseada na data de nascimento
   */
  calcularIdade(dataNascimento: string): number {
    const hoje = new Date();
    const nascimento = new Date(dataNascimento);
    let idade = hoje.getFullYear() - nascimento.getFullYear();
    const m = hoje.getMonth() - nascimento.getMonth();
    if (m < 0 || (m === 0 && hoje.getDate() < nascimento.getDate())) {
      idade--;
    }
    return idade;
  }

  /**
   * Valida se atleta atende critérios de idade da categoria
   */
  validarIdadeCategoria(dataNascimento: string, idadeMinima?: number, idadeMaxima?: number): boolean {
    const idade = this.calcularIdade(dataNascimento);
    
    if (idadeMinima && idade < idadeMinima) {
      return false;
    }
    
    if (idadeMaxima && idade > idadeMaxima) {
      return false;
    }
    
    return true;
  }

  /**
   * Valida se atleta atende critério de gênero da categoria
   */
  validarGeneroCategoria(genero: string, generoCategoria?: string): boolean {
    if (!generoCategoria || generoCategoria === 'MISTO') {
      return true;
    }
    return genero === generoCategoria;
  }
}