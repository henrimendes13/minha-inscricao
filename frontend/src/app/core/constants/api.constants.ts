export const API_CONFIG = {
  baseUrl: 'http://localhost:8080/api',
  endpoints: {
    auth: {
      login: '/auth/login',
      register: '/auth/register',
      refresh: '/auth/refresh',
      logout: '/auth/logout'
    },
    eventos: {
      base: '/eventos',
      byId: (id: number) => `/eventos/${id}`,
      status: (id: number) => `/eventos/${id}/status`,
      search: '/eventos/search'
    },
    usuarios: {
      base: '/usuarios',
      byId: (id: number) => `/usuarios/${id}`,
      profile: '/usuarios/profile'
    },
    inscricoes: {
      base: '/inscricoes',
      byId: (id: number) => `/inscricoes/${id}`,
      byEvento: (eventoId: number) => `/inscricoes/evento/${eventoId}`,
      byUsuario: (usuarioId: number) => `/inscricoes/usuario/${usuarioId}`
    },
    atletas: {
      base: '/atletas',
      byId: (id: number) => `/atletas/${id}`
    },
    equipes: {
      base: '/equipes',
      byId: (id: number) => `/equipes/${id}`
    },
    categorias: {
      base: '/categorias',
      byId: (id: number) => `/categorias/${id}`,
      byEvento: (eventoId: number) => `/categorias/evento/${eventoId}`
    },
    leaderboard: {
      base: '/leaderboard',
      byEventoAndCategoria: (eventoId: number, categoriaId: number) => 
        `/leaderboard/evento/${eventoId}/categoria/${categoriaId}`
    },
    workouts: {
      base: '/workouts',
      byId: (id: number) => `/workouts/${id}`,
      byEvento: (eventoId: number) => `/workouts/evento/${eventoId}`
    },
    timeline: {
      base: '/timeline',
      byId: (id: number) => `/timeline/${id}`,
      byEvento: (eventoId: number) => `/timeline/evento/${eventoId}`
    },
    anexos: {
      base: '/anexos',
      byId: (id: number) => `/anexos/${id}`,
      upload: '/anexos/upload'
    }
  }
};

export const HTTP_CONFIG = {
  timeout: 30000,
  retries: 3,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
};