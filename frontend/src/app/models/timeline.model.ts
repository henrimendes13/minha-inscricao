export interface Timeline {
  id: number;
  eventoId: number;
  nomeEvento: string;
  descricaoDiaUm: string | null;
  descricaoDiaDois: string | null;
  descricaoDiaTres: string | null;
  descricaoDiaQuatro: string | null;
  descricaoCompleta: string;
  totalDiasComDescricao: number;
  vazia: boolean;
  temDescricaoDiaUm: boolean;
  temDescricaoDiaDois: boolean;
  temDescricaoDiaTres: boolean;
  temDescricaoDiaQuatro: boolean;
  createdAt: string;
  updatedAt: string;
}
