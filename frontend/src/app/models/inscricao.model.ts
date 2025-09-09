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
