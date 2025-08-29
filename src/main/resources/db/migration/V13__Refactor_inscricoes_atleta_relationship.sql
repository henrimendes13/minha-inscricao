-- Migração V12: Refatoração do relacionamento Inscrições-Atleta
-- Descrição: Adiciona campo usuario_inscricao_id para separar quem fez a inscrição (usuario) do participante (atleta)
-- Data: 2025-01-XX

-- Passo 1: Adicionar nova coluna usuario_inscricao_id
ALTER TABLE inscricoes ADD COLUMN usuario_inscricao_id BIGINT;

-- Passo 2: Migrar dados existentes - assume que quem fez a inscrição foi o próprio atleta
-- Para registros com atleta_id = NULL, usar ID 1 como fallback (ajustar conforme necessário)
UPDATE inscricoes SET usuario_inscricao_id = COALESCE(atleta_id, 1);

-- Passo 3: Tornar a coluna obrigatória
ALTER TABLE inscricoes ALTER COLUMN usuario_inscricao_id SET NOT NULL;

-- Passo 4: Adicionar foreign key constraint para usuario_inscricao_id
ALTER TABLE inscricoes 
ADD CONSTRAINT fk_inscricoes_usuario_inscricao 
FOREIGN KEY (usuario_inscricao_id) REFERENCES usuarios(id);

-- Passo 5: Limpeza de dados órfãos
-- Limpar atleta_id que não existem na tabela atletas (dados órfãos de migrações anteriores)
UPDATE inscricoes SET atleta_id = NULL 
WHERE atleta_id IS NOT NULL 
  AND atleta_id NOT IN (SELECT id FROM atletas);

-- Passo 6: Modificar a foreign key existente de atleta_id para referenciar atletas ao invés de usuarios
-- Primeiro, remover a constraint existente se houver
DO $$ 
BEGIN
    -- Remove constraint de atleta_id se existir (pode ter nomes diferentes)
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE constraint_name LIKE '%atleta%' AND table_name = 'inscricoes') THEN
        ALTER TABLE inscricoes DROP CONSTRAINT IF EXISTS fk_inscricoes_atleta;
        ALTER TABLE inscricoes DROP CONSTRAINT IF EXISTS fk_atleta_id;
        ALTER TABLE inscricoes DROP CONSTRAINT IF EXISTS inscricoes_atleta_id_fkey;
    END IF;
END $$;

-- Passo 7: Adicionar nova constraint para atleta_id referenciar tabela atletas
ALTER TABLE inscricoes 
ADD CONSTRAINT fk_inscricoes_atleta 
FOREIGN KEY (atleta_id) REFERENCES atletas(id);

-- Passo 8: Adicionar índices para performance
CREATE INDEX IF NOT EXISTS idx_inscricoes_usuario_inscricao_id ON inscricoes(usuario_inscricao_id);
CREATE INDEX IF NOT EXISTS idx_inscricoes_atleta_id ON inscricoes(atleta_id);

-- Passo 9: Adicionar comentários para documentação
COMMENT ON COLUMN inscricoes.usuario_inscricao_id IS 'ID do usuário que realizou a inscrição (quem fez login e efetuou a inscrição)';
COMMENT ON COLUMN inscricoes.atleta_id IS 'ID do atleta que irá participar do evento (pode ser diferente de quem fez a inscrição)';

-- Verificação de integridade dos dados
DO $$ 
DECLARE 
    total_inscricoes INTEGER;
    inscricoes_sem_usuario INTEGER;
    inscricoes_sem_atleta INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_inscricoes FROM inscricoes;
    SELECT COUNT(*) INTO inscricoes_sem_usuario FROM inscricoes WHERE usuario_inscricao_id IS NULL;
    SELECT COUNT(*) INTO inscricoes_sem_atleta FROM inscricoes WHERE atleta_id IS NULL;
    
    RAISE NOTICE 'Migração V12 concluída:';
    RAISE NOTICE 'Total de inscrições: %', total_inscricoes;
    RAISE NOTICE 'Inscrições sem usuário: %', inscricoes_sem_usuario;
    RAISE NOTICE 'Inscrições sem atleta: %', inscricoes_sem_atleta;
    
    IF inscricoes_sem_usuario > 0 OR inscricoes_sem_atleta > 0 THEN
        RAISE WARNING 'ATENÇÃO: Existem inscrições com dados incompletos!';
    ELSE
        RAISE NOTICE 'Migração concluída com sucesso!';
    END IF;
END $$;