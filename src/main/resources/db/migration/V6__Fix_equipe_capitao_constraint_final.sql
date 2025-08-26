-- Migração V6: Correção final do relacionamento capitão da equipe
-- Descrição: Remove qualquer constraint antiga e cria a nova corretamente
-- Data: 2025-08-26

-- Remove todas as constraints antigas de capitao_id que referenciam usuarios
DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    -- Remove qualquer constraint que referencie usuarios
    FOR constraint_record IN 
        SELECT tc.constraint_name 
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu 
            ON tc.constraint_name = kcu.constraint_name
        JOIN information_schema.constraint_column_usage ccu 
            ON tc.constraint_name = ccu.constraint_name
        WHERE tc.table_name = 'equipes' 
            AND tc.constraint_type = 'FOREIGN KEY'
            AND kcu.column_name = 'capitao_id'
            AND ccu.table_name = 'usuarios'
    LOOP
        EXECUTE format('ALTER TABLE equipes DROP CONSTRAINT IF EXISTS %I', constraint_record.constraint_name);
        RAISE NOTICE 'Removida constraint: %', constraint_record.constraint_name;
    END LOOP;
END $$;

-- Remove tabela de relacionamento equipe_usuarios se ainda existir
DROP TABLE IF EXISTS equipe_usuarios;

-- Remove constraint existente se houver conflito de nome
ALTER TABLE equipes DROP CONSTRAINT IF EXISTS FK_equipes_capitao_atletas;

-- Cria a nova constraint referenciando atletas
ALTER TABLE equipes ADD CONSTRAINT FK_equipes_capitao_atletas 
FOREIGN KEY (capitao_id) REFERENCES atletas(id);

-- Adiciona comentário explicativo
COMMENT ON CONSTRAINT FK_equipes_capitao_atletas ON equipes IS 
'Capitão da equipe deve ser um dos atletas participantes (AtletaEntity)';

-- Adiciona índice para performance
CREATE INDEX IF NOT EXISTS idx_equipes_capitao_id ON equipes(capitao_id);