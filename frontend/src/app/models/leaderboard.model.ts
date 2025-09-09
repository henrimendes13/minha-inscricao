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

// Novas interfaces baseadas no backend
export interface WorkoutPosicao {
  workoutId: number;
  nomeWorkout: string;
  posicaoWorkout: number;
  resultadoFormatado: string;
}

export interface LeaderboardRanking {
  posicao: number;
  nomeParticipante: string;
  pontuacaoTotal: number;
  isEquipe: boolean;
  participanteId: number;
  nomeCategoria: string;
  workoutsCompletados: number;
  isPodio: boolean;
  medalha: string;
  posicoesWorkouts: WorkoutPosicao[];
}

export interface LeaderboardRankingResponse {
  rankings: LeaderboardRanking[];
  categoria: string;
  totalParticipantes: number;
  workouts: string[];
}
