export interface EquipeCreateDTO {
  nome: string;
  descricao?: string;
  atletaIds: number[];
}

export interface EquipeUpdateDTO {
  nome?: string;
  descricao?: string;
  atletaIds?: number[];
}

export interface EquipeResponseDTO {
  id: number;
  nome: string;
  descricao?: string;
  atletaIds: number[];
  usuarioId: number;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface EquipeSummaryDTO {
  id: number;
  nome: string;
  descricao?: string;
  numeroAtletas: number;
}