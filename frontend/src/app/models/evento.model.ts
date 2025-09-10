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

export enum StatusEvento {
  PLANEJADO = 'PLANEJADO',
  INSCRICOES_ABERTAS = 'INSCRICOES_ABERTAS',
  INSCRICOES_FECHADAS = 'INSCRICOES_FECHADAS',
  EM_ANDAMENTO = 'EM_ANDAMENTO',
  FINALIZADO = 'FINALIZADO',
  CANCELADO = 'CANCELADO'
}

// Interface para a resposta da API real
export interface EventoApiResponse {
  id: number;
  nome: string;
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
