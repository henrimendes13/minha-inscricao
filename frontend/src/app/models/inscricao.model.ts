// Import types from existing models
import { Genero } from './atleta.model';

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

export enum StatusInscricao {
  PENDENTE = 'PENDENTE',
  CONFIRMADA = 'CONFIRMADA',
  CANCELADA = 'CANCELADA',
  PAGAMENTO_PENDENTE = 'PAGAMENTO_PENDENTE',
  PAGO = 'PAGO'
}

export enum TipoParticipacao {
  INDIVIDUAL = 'INDIVIDUAL',
  EQUIPE = 'EQUIPE'
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