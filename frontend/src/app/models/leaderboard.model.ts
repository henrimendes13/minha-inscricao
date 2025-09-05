export interface LeaderboardEntry {
  id: number;
  eventoId: number;
  categoriaId: number;
  atletaId?: number;
  equipeId?: number;
  nome: string;
  posicao: number;
  pontuacao: number;
  tempo?: string;
  detalhes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LeaderboardResponse {
  entries: LeaderboardEntry[];
  categoria: string;
  totalParticipantes: number;
}

export interface Categoria {
  id: number;
  nome: string;
  descricao?: string;
  eventoId: number;
}