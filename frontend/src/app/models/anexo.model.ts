// Interface baseada no AnexoSummaryDTO do backend
export interface Anexo {
  id: number;
  nomeArquivo: string;
  descricao: string;
  extensao: string;
  tamanhoFormatado: string;
  ativo: boolean;
  tipoMime: string;
}

// Interface para resposta completa (se necessário no futuro)
export interface AnexoDetailed {
  id: number;
  nomeArquivo: string;
  descricao: string;
  caminhoArquivo: string;
  tipoMime: string;
  tamanhoBytes: number;
  tamanhoFormatado: string;
  extensao: string;
  checksumMd5: string;
  ativo: boolean;
  nomeEvento: string;
  eventoId: number;
  isImagem: boolean;
  isPdf: boolean;
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

// Response do backend - retorna List<AnexoSummaryDTO> diretamente
export type AnexoResponse = Anexo[];
