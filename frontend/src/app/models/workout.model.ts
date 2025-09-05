export interface Workout {
  id: number;
  eventoId: number;
  nome: string;
  descricao?: string;
  tipo: WorkoutType;
  dataHora?: string;
  duracao?: number;
  instrucoes?: string;
  equipamentos?: string[];
  ordem: number;
  ativo: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum WorkoutType {
  WOD = 'WOD',
  METCON = 'METCON',
  STRENGTH = 'STRENGTH',
  CARDIO = 'CARDIO',
  SKILL = 'SKILL',
  CUSTOM = 'CUSTOM'
}

export interface WorkoutResponse {
  workouts: Workout[];
  total: number;
}