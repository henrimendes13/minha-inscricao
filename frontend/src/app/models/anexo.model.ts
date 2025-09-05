export interface Anexo {
  id: number;
  eventoId: number;
  nome: string;
  descricao?: string;
  url: string;
  tipo: AnexoType;
  tamanho: number;
  extensao: string;
  publico: boolean;
  ordem: number;
  createdAt: string;
  updatedAt: string;
}

export enum AnexoType {
  REGULAMENTO = 'REGULAMENTO',
  EDITAL = 'EDITAL',
  CRONOGRAMA = 'CRONOGRAMA',
  MAPA = 'MAPA',
  FOTO = 'FOTO',
  VIDEO = 'VIDEO',
  DOCUMENTO = 'DOCUMENTO',
  OUTROS = 'OUTROS'
}

export interface AnexoResponse {
  anexos: Anexo[];
  total: number;
}