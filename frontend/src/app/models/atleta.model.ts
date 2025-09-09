export interface AtletaCreateDTO {
  nome: string;
  cpf: string;
  dataNascimento: string;
  genero: Genero;
  email: string;
  telefone?: string;
  endereco?: string;
  emergenciaContato?: string;
  emergenciaTelefone?: string;
  observacoesMedicas?: string;
}

export interface AtletaUpdateDTO {
  nome?: string;
  email?: string;
  telefone?: string;
  endereco?: string;
  emergenciaContato?: string;
  emergenciaTelefone?: string;
  observacoesMedicas?: string;
}

export interface AtletaResponseDTO {
  id: number;
  nome: string;
  cpf: string;
  dataNascimento: string;
  genero: Genero;
  email: string;
  telefone?: string;
  endereco?: string;
  emergenciaContato?: string;
  emergenciaTelefone?: string;
  observacoesMedicas?: string;
  usuarioId: number;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface AtletaSummaryDTO {
  id: number;
  nome: string;
  email: string;
  genero: Genero;
  dataNascimento: string;
}

export enum Genero {
  MASCULINO = 'MASCULINO',
  FEMININO = 'FEMININO',
  OUTRO = 'OUTRO'
}
