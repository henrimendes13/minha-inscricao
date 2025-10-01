import { TipoParticipacao } from './inscricao.model';

/**
 * Enum de Gênero
 */
export enum Genero {
  MASCULINO = 'MASCULINO',
  FEMININO = 'FEMININO'
}

/**
 * Request para criar nova categoria
 */
export interface CategoriaCreateRequest {
  nome: string;
  descricao?: string;
  idadeMinima?: number;
  idadeMaxima?: number;
  genero?: Genero | null;
  tipoParticipacao: TipoParticipacao;
  quantidadeDeAtletasPorEquipe?: number;
  valorInscricao: number;
  ativa?: boolean;
}

/**
 * Request para atualizar categoria existente
 */
export interface CategoriaUpdateRequest {
  nome?: string;
  descricao?: string;
  idadeMinima?: number;
  idadeMaxima?: number;
  genero?: Genero | null;
  tipoParticipacao?: TipoParticipacao;
  quantidadeDeAtletasPorEquipe?: number;
  valorInscricao?: number;
  ativa?: boolean;
}

/**
 * Resposta completa da API para categoria
 */
export interface CategoriaApiResponse {
  id: number;
  eventoId: number;
  nomeEvento: string;
  nome: string;
  descricao?: string;
  idadeMinima?: number;
  idadeMaxima?: number;
  genero?: Genero;
  tipoParticipacao: TipoParticipacao;
  quantidadeDeAtletasPorEquipe?: number;
  valorInscricao: number;
  ativa: boolean;
  numeroInscricoesAtivas: number;
  numeroEquipesAtivas: number;
  totalEquipes: number;
  descricaoCompleta: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Resposta resumida da API para categoria (usada em listas)
 * Matches backend CategoriaSummaryDTO
 */
export interface CategoriaSummaryResponse {
  id: number;
  nome: string;
  nomeEvento: string;
  genero?: Genero;
  tipoParticipacao: TipoParticipacao;
  quantidadeDeAtletasPorEquipe?: number;
  valorInscricao: number;
  ativa: boolean;
  numeroInscricoesAtivas: number;
  numeroEquipesAtivas: number;
  descricaoCompleta: string;
  createdAt: string;
}

/**
 * Helper para obter label do gênero
 */
export function getGeneroLabel(genero: Genero | null | undefined): string {
  if (!genero) return 'Misto';
  return genero === Genero.MASCULINO ? 'Masculino' : 'Feminino';
}

/**
 * Helper para obter label do tipo de participação
 */
export function getTipoParticipacaoLabel(tipo: TipoParticipacao): string {
  return tipo === TipoParticipacao.INDIVIDUAL ? 'Individual' : 'Equipe';
}

/**
 * Helper para formatar faixa etária
 */
export function formatarFaixaEtaria(idadeMin?: number, idadeMax?: number): string {
  if (!idadeMin && !idadeMax) return 'Livre';
  if (idadeMin && !idadeMax) return `${idadeMin}+`;
  if (!idadeMin && idadeMax) return `Até ${idadeMax} anos`;
  return `${idadeMin} a ${idadeMax} anos`;
}
