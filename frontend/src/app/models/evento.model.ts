// Interface alinhada com EventoCreateDTO do backend
export interface EventoCreateRequest {
  nome: string;
  dataInicioDoEvento: string; // formato: dd-MM-yyyy
  dataFimDoEvento: string;    // formato: dd-MM-yyyy
  descricao?: string;
  cidade?: string;
  estado?: string;
  endereco?: string;
}

// Interface alinhada com EventoUpdateDTO do backend
export interface EventoUpdateRequest {
  nome: string;
  dataInicioDoEvento: string; // formato: dd-MM-yyyy
  dataFimDoEvento: string;    // formato: dd-MM-yyyy
  descricao?: string;
  cidade?: string;
  estado?: string;
  endereco?: string;
}

// Enum de status alinhado com o backend
export enum StatusEvento {
  RASCUNHO = 'RASCUNHO',
  ABERTO = 'ABERTO',
  INSCRICOES_ENCERRADAS = 'INSCRICOES_ENCERRADAS',
  EM_ANDAMENTO = 'EM_ANDAMENTO',
  FINALIZADO = 'FINALIZADO',
  CANCELADO = 'CANCELADO',
  ADIADO = 'ADIADO'
}

// Legacy DTOs (manter para compatibilidade)
export interface EventoCreateDTO {
  nome: string;
  descricao?: string;
  dataInicio: string;
  dataFim: string;
  dataInicioInscricoes: string;
  dataFimInscricoes: string;
  local?: string;
  organizadorId: number;
  limitarInscricoes?: boolean;
  maxInscricoes?: number;
  valorInscricao?: number;
  informacoesAdicionais?: string;
}

export interface EventoUpdateDTO {
  nome?: string;
  descricao?: string;
  dataInicio?: string;
  dataFim?: string;
  dataInicioInscricoes?: string;
  dataFimInscricoes?: string;
  local?: string;
  limitarInscricoes?: boolean;
  maxInscricoes?: number;
  valorInscricao?: number;
  informacoesAdicionais?: string;
}

export interface EventoResponseDTO {
  id: number;
  nome: string;
  descricao?: string;
  dataInicio: string;
  dataFim: string;
  dataInicioInscricoes: string;
  dataFimInscricoes: string;
  local?: string;
  organizadorId: number;
  limitarInscricoes: boolean;
  maxInscricoes?: number;
  valorInscricao?: number;
  informacoesAdicionais?: string;
  status: StatusEvento;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface EventoSummaryDTO {
  id: number;
  nome: string;
  dataInicio: string;
  dataFim: string;
  local?: string;
  status: StatusEvento;
}

// Interface para a resposta da API real
export interface EventoApiResponse {
  id: number;
  nome: string;
  descricao?: string;
  dataInicioDoEvento: string;
  dataFimDoEvento: string;
  status: string;
  descricaoStatus: string;
  nomeOrganizador: string;
  organizadorEmail: string;
  totalCategorias: number;
  inscricoesAtivas: number;
  podeReceberInscricoes: boolean;
  createdAt: string;
  endereco: string;
  cidade: string;
  estado: string;
  imagemUrl?: string;
}
