// Import types from existing models
import { Genero } from './atleta.model';

// Atualizado para refletir backend DTOs
export interface InscricaoCreateRequest {
  usuarioInscricaoId: number;
  eventoId: number;
  categoriaId: number;
  atletaId?: number;
  equipeId?: number;
  valor: number;
  codigoDesconto?: string;
  valorDesconto?: number;
  termosAceitos: boolean;
  observacoes?: string;
}

export interface InscricaoUpdateRequest {
  categoriaId?: number;
  status?: StatusInscricao;
  valor?: number;
  termosAceitos?: boolean;
  codigoDesconto?: string;
  valorDesconto?: number;
  motivoCancelamento?: string;
}

export interface AtletaSummary {
  id: number;
  nome: string;
  dataNascimento: string;
  genero: string;
  telefone: string;
  aceitaTermos: boolean;
  idade: number;
  podeParticipar: boolean;
  nomeEvento: string;
  statusInscricao: string;
  nomeEquipe?: string;
}

export interface InscricaoDetailedResponse {
  id: number;
  atletas: AtletaSummary[];
  usuarioInscricaoId: number;
  nomeUsuarioInscricao: string;
  eventoId: number;
  nomeEvento: string;
  categoriaId: number;
  nomeCategoria: string;
  equipeId?: number;
  nomeEquipe?: string;
  status: StatusInscricao;
  descricaoStatus: string;
  valor: number;
  dataInscricao: string;
  dataConfirmacao?: string;
  dataCancelamento?: string;
  termosAceitos: boolean;
  codigoDesconto?: string;
  valorDesconto?: number;
  motivoCancelamento?: string;
  createdAt: string;
  updatedAt: string;
  valorTotal: number;
  temDesconto: boolean;
  podeSerCancelada: boolean;
  precisaPagamento: boolean;
  ativa: boolean;
  tipoInscricao: string;
  numeroParticipantes: number;
  nomeParticipante: string;
  temPagamento: boolean;
}

export interface InscricaoSummaryResponse {
  id: number;
  nomeEvento: string;
  nomeCategoria: string;
  nomeEquipe?: string;
  status: StatusInscricao;
  descricaoStatus: string;
  valorTotal: number;
  dataInscricao: string;
  dataConfirmacao?: string;
  tipoInscricao: string;
  numeroParticipantes: number;
  nomeParticipante: string;
  ativa: boolean;
  podeSerCancelada: boolean;
  precisaPagamento: boolean;
}

export enum StatusInscricao {
  PENDENTE = 'PENDENTE',
  CONFIRMADA = 'CONFIRMADA',
  CANCELADA = 'CANCELADA',
  RECUSADA = 'RECUSADA',
  EXPIRADA = 'EXPIRADA',
  LISTA_ESPERA = 'LISTA_ESPERA'
}

export enum TipoParticipacao {
  INDIVIDUAL = 'INDIVIDUAL',
  EQUIPE = 'EQUIPE'
}

// Funções utilitárias
export function getStatusLabel(status: StatusInscricao): string {
  const labels: Record<StatusInscricao, string> = {
    [StatusInscricao.PENDENTE]: 'Pendente',
    [StatusInscricao.CONFIRMADA]: 'Confirmada',
    [StatusInscricao.CANCELADA]: 'Cancelada',
    [StatusInscricao.RECUSADA]: 'Recusada',
    [StatusInscricao.EXPIRADA]: 'Expirada',
    [StatusInscricao.LISTA_ESPERA]: 'Lista de Espera'
  };
  return labels[status] || status;
}

export function getStatusColor(status: StatusInscricao): string {
  const colors: Record<StatusInscricao, string> = {
    [StatusInscricao.PENDENTE]: 'warn',
    [StatusInscricao.CONFIRMADA]: 'primary',
    [StatusInscricao.CANCELADA]: 'accent',
    [StatusInscricao.RECUSADA]: 'basic',
    [StatusInscricao.EXPIRADA]: 'basic',
    [StatusInscricao.LISTA_ESPERA]: 'primary'
  };
  return colors[status] || 'basic';
}

// Legacy interfaces (manter para compatibilidade com outros componentes)
export interface InscricaoCreateDTO {
  eventoId: number;
  categoriaId: number;
  equipeId?: number;
  atletaId?: number;
  tipoParticipacao: TipoParticipacao;
  observacoes?: string;
}

export interface InscricaoUpdateDTO {
  status?: StatusInscricao;
  observacoes?: string;
}

export interface InscricaoResponseDTO {
  id: number;
  eventoId: number;
  categoriaId: number;
  equipeId?: number;
  atletaId?: number;
  tipoParticipacao: TipoParticipacao;
  status: StatusInscricao;
  observacoes?: string;
  dataInscricao: string;
  dataAtualizacao: string;
}

export interface InscricaoSummaryDTO {
  id: number;
  eventoNome: string;
  categoriaNome: string;
  status: StatusInscricao;
  dataInscricao: string;
  tipoParticipacao: TipoParticipacao;
}

// Inscription-specific DTOs
export interface AtletaInscricaoDTO {
  nome: string;
  cpf?: string;
  dataNascimento: string;
  genero: Genero;
  telefone?: string;
  emergenciaNome?: string;
  emergenciaTelefone?: string;
  observacoesMedicas?: string;
  endereco?: string;
  email?: string;
  aceitaTermos: boolean;
  categoriaId: number;
  valorInscricao?: number;
  codigoDesconto?: string;
  termosInscricaoAceitos: boolean;
}

export interface EquipeInscricaoDTO {
  nome: string;
  categoriaId: number;
  atletas: AtletaInscricaoDTO[];
  capitaoCpf?: string;
  valorInscricao?: number;
  codigoDesconto?: string;
  termosAceitos: boolean;
}

export interface CategoriaEscolhida {
  categoriaId: number;
  quantidade: number;
}

export interface InscricaoFormData {
  eventoId: number;
  categorias: CategoriaEscolhida[];
  tipoInscricao: TipoParticipacao;
  valorTotal: number;
}
