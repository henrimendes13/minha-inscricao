-- Migração V11: Alterar tipo da coluna pontuacao_total para INTEGER
-- Descrição: Converte pontuacao_total de DECIMAL para INTEGER nas tabelas atletas e equipes

-- Alterar tipo da coluna pontuacao_total na tabela atletas
ALTER TABLE atletas 
ALTER COLUMN pontuacao_total TYPE INTEGER 
USING FLOOR(pontuacao_total)::INTEGER;

-- Alterar tipo da coluna pontuacao_total na tabela equipes  
ALTER TABLE equipes 
ALTER COLUMN pontuacao_total TYPE INTEGER 
USING FLOOR(pontuacao_total)::INTEGER;

-- Atualizar comentários das colunas
COMMENT ON COLUMN atletas.pontuacao_total IS 'Pontuação total do atleta (inteiro) calculada pela soma das posições nos workouts';
COMMENT ON COLUMN equipes.pontuacao_total IS 'Pontuação total da equipe (inteiro) calculada pela soma das posições nos workouts';

-- Garantir que não há valores NULL
UPDATE atletas SET pontuacao_total = 0 WHERE pontuacao_total IS NULL;
UPDATE equipes SET pontuacao_total = 0 WHERE pontuacao_total IS NULL;

-- Adicionar constraint NOT NULL após garantir que não há valores NULL
ALTER TABLE atletas ALTER COLUMN pontuacao_total SET NOT NULL;
ALTER TABLE equipes ALTER COLUMN pontuacao_total SET NOT NULL;

-- Adicionar valor padrão
ALTER TABLE atletas ALTER COLUMN pontuacao_total SET DEFAULT 0;
ALTER TABLE equipes ALTER COLUMN pontuacao_total SET DEFAULT 0;