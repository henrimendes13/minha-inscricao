export interface TimelineItem {
  id: number;
  eventoId: number;
  titulo: string;
  descricao?: string;
  dataHora: string;
  tipo: TimelineType;
  icone?: string;
  cor?: string;
  createdAt: string;
  updatedAt: string;
}

export enum TimelineType {
  INSCRICOES_ABERTAS = 'INSCRICOES_ABERTAS',
  INSCRICOES_FECHADAS = 'INSCRICOES_FECHADAS',
  INICIO_EVENTO = 'INICIO_EVENTO',
  FIM_EVENTO = 'FIM_EVENTO',
  DIVULGACAO_RESULTADO = 'DIVULGACAO_RESULTADO',
  CUSTOM = 'CUSTOM'
}

export interface TimelineResponse {
  items: TimelineItem[];
  total: number;
}