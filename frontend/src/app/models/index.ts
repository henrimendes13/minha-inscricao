export * from './evento.model';
export * from './usuario.model';
export * from './atleta.model';
export * from './equipe.model';

// Export only specific interfaces from inscricao.model to avoid conflicts
export type {
  InscricaoCreateDTO,
  InscricaoUpdateDTO,
  InscricaoResponseDTO,
  InscricaoSummaryDTO,
  AtletaInscricaoDTO,
  EquipeInscricaoDTO,
  CategoriaEscolhida,
  InscricaoFormData
} from './inscricao.model';

export {
  StatusInscricao,
  TipoParticipacao
} from './inscricao.model';
