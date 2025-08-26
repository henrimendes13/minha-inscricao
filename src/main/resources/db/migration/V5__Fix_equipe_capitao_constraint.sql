-- Migração V5: Correção do relacionamento capitão da equipe
-- Descrição: Altera constraint de capitao_id para referenciar atletas em vez de usuarios
-- Data: 2025-08-26

-- Remove constraints antigas que referenciam usuarios
ALTER TABLE equipes DROP CONSTRAINT IF EXISTS FK1v2rdhp6k64ypccnj4t2p3u6m;
ALTER TABLE equipes DROP CONSTRAINT IF EXISTS fk8e27wg4eh4p7txubx0eaduo60;

-- Remove qualquer outra constraint de capitao_id que aponte para usuarios (segurança adicional)
DO $$
DECLARE
    constraint_record RECORD;
BEGIN
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
        RAISE NOTICE 'Removida constraint adicional: %', constraint_record.constraint_name;
    END LOOP;
END $$;

-- Remove tabela de relacionamento equipe_usuarios que não é mais usada
-- (agora equipes têm relacionamento direto com atletas via AtletaEntity)
DROP TABLE IF EXISTS equipe_usuarios;

-- Adiciona nova constraint que referencia atletas (se não existir)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_equipes_capitao_atletas' 
        AND table_name = 'equipes'
    ) THEN
        ALTER TABLE equipes ADD CONSTRAINT FK_equipes_capitao_atletas 
        FOREIGN KEY (capitao_id) REFERENCES atletas(id);
    END IF;
END $$;

-- Adiciona comentário explicativo
COMMENT ON CONSTRAINT FK_equipes_capitao_atletas ON equipes IS 
'Capitão da equipe deve ser um dos atletas participantes (AtletaEntity)';

-- Adiciona índice para performance
CREATE INDEX IF NOT EXISTS idx_equipes_capitao_id ON equipes(capitao_id);