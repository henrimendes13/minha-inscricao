// Interface baseada no WorkoutSummaryDTO do backend
export interface Workout {
  id: number;
  nome: string;
  tipo: WorkoutType;
  ativo: boolean;
  quantidadeCategorias: number;
  nomesCategorias: string;
  nomeEvento: string;
  unidadeMedida: string;
  descricao: string;
}

// Interface baseada no WorkoutResponseDTO do backend (para detalhes)
export interface WorkoutDetailed {
  id: number;
  nome: string;
  descricao: string;
  tipo: WorkoutType;
  ativo: boolean;
  evento: EventoSummary;
  categorias: CategoriaSummary[];
  quantidadeCategorias: number;
  nomesCategorias: string;
  unidadeMedida: string;
  createdAt: string;
  updatedAt: string;
}

export interface EventoSummary {
  id: number;
  nome: string;
}

export interface CategoriaSummary {
  id: number;
  nome: string;
  ativa: boolean;
}

export enum WorkoutType {
  REPS = 'REPS',
  PESO = 'PESO',
  TEMPO = 'TEMPO'
}

// Interface para agrupamento por categoria
export interface WorkoutsByCategory {
  categoria: CategoriaSummary;
  workouts: Workout[];
}

// Response do backend - retorna List<WorkoutSummaryDTO> diretamente
export type WorkoutResponse = Workout[];

// Interfaces para gerenciamento de resultados
export interface WorkoutResultCreateDTO {
  eventoId: number;
  categoriaId: number;
  participanteId: number;
  isEquipe: boolean;
  resultadoValor: string;
  finalizado: boolean;
}

export interface WorkoutResultUpdateDTO {
  resultadoValor: string;
  finalizado: boolean;
}

export interface WorkoutResultStatusDTO {
  workoutId: number;
  nomeWorkout: string;
  categoriaId: number;
  totalParticipantes: number;
  participantesFinalizados: number;
  porcentagemFinalizados: number;
  workoutFinalizado: boolean;
  participantesPendentes: string[];
}

export interface LeaderboardSummaryDTO {
  id: number;
  eventoId: number;
  workoutId: number;
  categoriaId: number;
  atletaId?: number;
  equipeId?: number;
  nomeParticipante: string;
  isEquipe: boolean;
  resultadoValor: string;
  resultadoFormatado: string;
  posicaoWorkout: number;
  pontuacaoWorkout: number;
  finalizado: boolean;
  createdAt: string;
  updatedAt?: string;
}

export interface LeaderboardResponseDTO {
  id: number;
  eventoId: number;
  workoutId: number;
  categoriaId: number;
  atletaId?: number;
  equipeId?: number;
  nomeParticipante: string;
  isEquipe: boolean;
  resultadoValor: string;
  resultadoFormatado: string;
  posicaoWorkout: number;
  pontuacaoWorkout: number;
  finalizado: boolean;
  createdAt: string;
  updatedAt?: string;
}
