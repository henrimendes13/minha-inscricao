-- Migração V9: Refatoração da pontuação total
-- Move pontuacao_total de leaderboards para atletas e equipes
-- Descrição: Melhora performance e consistência do sistema de ranking

-- Adicionar coluna pontuacao_total na tabela atletas
ALTER TABLE atletas 
ADD COLUMN pontuacao_total NUMERIC(10,2) DEFAULT 0.00;

-- Adicionar coluna pontuacao_total na tabela equipes
ALTER TABLE equipes 
ADD COLUMN pontuacao_total NUMERIC(10,2) DEFAULT 0.00;

-- Migrar dados existentes - Calcular pontuação total para atletas
UPDATE atletas 
SET pontuacao_total = COALESCE((
    SELECT SUM(l.posicao_workout) 
    FROM leaderboards l 
    WHERE l.atleta_id = atletas.id 
    AND l.posicao_workout IS NOT NULL
), 0.00);

-- Migrar dados existentes - Calcular pontuação total para equipes
UPDATE equipes 
SET pontuacao_total = COALESCE((
    SELECT SUM(l.posicao_workout) 
    FROM leaderboards l 
    WHERE l.equipe_id = equipes.id 
    AND l.posicao_workout IS NOT NULL
), 0.00);

-- Remover a coluna pontuacao_total da tabela leaderboards
-- (será feito após atualizar o código para evitar problemas de compatibilidade)
-- ALTER TABLE leaderboards DROP COLUMN pontuacao_total;

-- Adicionar comentários nas colunas
COMMENT ON COLUMN atletas.pontuacao_total IS 'Pontuação total do atleta calculada pela soma das posições nos workouts';
COMMENT ON COLUMN equipes.pontuacao_total IS 'Pontuação total da equipe calculada pela soma das posições nos workouts';

-- Criar índices para melhor performance nas consultas de ranking
CREATE INDEX idx_atletas_pontuacao_total ON atletas(pontuacao_total);
CREATE INDEX idx_equipes_pontuacao_total ON equipes(pontuacao_total);

-- Adicionar índices compostos para queries de ranking por categoria/evento
CREATE INDEX idx_atletas_categoria_pontuacao ON atletas(categoria_id, pontuacao_total);
CREATE INDEX idx_equipes_categoria_pontuacao ON equipes(categoria_id, pontuacao_total);