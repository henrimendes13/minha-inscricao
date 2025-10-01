export interface DashboardEstatisticas {
  eventos: EventoEstatisticas;
  inscricoes: InscricaoEstatisticas;
  usuarios: UsuarioEstatisticas;
  financeiro: FinanceiroEstatisticas;
  inscricoesPorMes: EstatisticaPorMes[];
  eventosMaisPopulares: EventoPopular[];
}

export interface EventoEstatisticas {
  total: number;
  rascunho: number;
  publicado: number;
  inscricoesAbertas: number;
  inscricoesEncerradas: number;
  emAndamento: number;
  concluido: number;
  cancelado: number;
}

export interface InscricaoEstatisticas {
  total: number;
  pendente: number;
  confirmada: number;
  cancelada: number;
  aguardandoPagamento: number;
  expirada: number;
}

export interface UsuarioEstatisticas {
  total: number;
  admins: number;
  organizadores: number;
  atletas: number;
  ativos: number;
  inativos: number;
}

export interface FinanceiroEstatisticas {
  receitaTotal: number;
  receitaPendente: number;
  inscricoesPagas: number;
  inscricoesPendentes: number;
}

export interface EstatisticaPorMes {
  mes: string;
  mesNome: string;
  quantidade: number;
}

export interface EventoPopular {
  id: number;
  nome: string;
  totalInscricoes: number;
  status: string;
}
