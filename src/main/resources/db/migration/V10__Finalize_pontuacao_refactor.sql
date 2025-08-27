-- Migração V10: Finalizar refatoração da pontuação total
-- Remove a coluna pontuacao_total da tabela leaderboards após migração dos dados

-- Verificar se ainda existem dados na coluna antes de remover
-- (apenas como segurança - os dados já foram migrados na V9)

-- Remover a coluna pontuacao_total da tabela leaderboards
ALTER TABLE leaderboards DROP COLUMN IF EXISTS pontuacao_total;

-- Opcional: Recalcular todas as pontuações para garantir consistência
-- (este script pode ser executado manualmente se necessário)

-- Função para recalcular pontuação de atletas
-- UPDATE atletas 
-- SET pontuacao_total = COALESCE((
--     SELECT SUM(l.posicao_workout) 
--     FROM leaderboards l 
--     WHERE l.atleta_id = atletas.id 
--     AND l.posicao_workout IS NOT NULL
-- ), 0.00)
-- WHERE categoria_id IS NOT NULL;

-- Função para recalcular pontuação de equipes
-- UPDATE equipes 
-- SET pontuacao_total = COALESCE((
--     SELECT SUM(l.posicao_workout) 
--     FROM leaderboards l 
--     WHERE l.equipe_id = equipes.id 
--     AND l.posicao_workout IS NOT NULL
-- ), 0.00);

-- Adicionar comentário para documentar a mudança
COMMENT ON TABLE atletas IS 'Tabela de atletas - pontuacao_total adicionada na V9 para melhor performance';
COMMENT ON TABLE equipes IS 'Tabela de equipes - pontuacao_total adicionada na V9 para melhor performance';