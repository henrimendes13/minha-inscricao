export interface UsuarioCreateDTO {
  nome: string;
  email: string;
  senha: string;
  tipoUsuario: TipoUsuario;
}

export interface UsuarioUpdateDTO {
  nome?: string;
  email?: string;
  senha?: string;
}

export interface UsuarioResponseDTO {
  id: number;
  nome: string;
  email: string;
  tipoUsuario: TipoUsuario;
  ativo: boolean;
  dataCriacao: string;
  dataAtualizacao: string;
}

export interface UsuarioSummaryDTO {
  id: number;
  nome: string;
  email: string;
  tipoUsuario: TipoUsuario;
}

export interface UsuarioComOrganizadorCreateDTO {
  nome: string;
  email: string;
  senha: string;
  organizador: {
    nomeOrganizacao: string;
    descricao?: string;
    site?: string;
    telefone?: string;
  };
}

export interface UsuarioComOrganizadorResponseDTO {
  usuario: UsuarioResponseDTO;
  organizador: {
    id: number;
    nomeOrganizacao: string;
    descricao?: string;
    site?: string;
    telefone?: string;
  };
}

export enum TipoUsuario {
  ORGANIZADOR = 'ORGANIZADOR',
  ATLETA = 'ATLETA',
  ADMIN = 'ADMIN'
}
