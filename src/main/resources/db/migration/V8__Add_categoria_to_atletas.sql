-- Migração V8: Adicionar categoria_id à tabela atletas
-- Descrição: Permite buscar atletas diretamente por categoria sem necessidade de JOIN com inscrições

-- Adicionar coluna categoria_id à tabela atletas se não existir
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'atletas' AND column_name = 'categoria_id') THEN
        ALTER TABLE atletas ADD COLUMN categoria_id bigint;
    END IF;
END
$$;

-- Adicionar foreign key constraint se não existir
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'fk_atleta_categoria' AND table_name = 'atletas') THEN
        ALTER TABLE atletas ADD CONSTRAINT fk_atleta_categoria 
            FOREIGN KEY (categoria_id) REFERENCES categorias(id);
    END IF;
END
$$;

-- Popular categoria_id baseado nas inscrições existentes
-- Para atletas individuais (inscricao.atleta_id aponta para usuarios, mas precisamos mapear para atletas via CPF)
UPDATE atletas SET categoria_id = (
    SELECT i.categoria_id 
    FROM inscricoes i 
    JOIN usuarios u ON i.atleta_id = u.id 
    WHERE u.cpf = atletas.cpf 
    AND i.equipe_id IS NULL
    LIMIT 1
) WHERE categoria_id IS NULL;

-- Para atletas de equipe (via equipe.categoria_id)
UPDATE atletas SET categoria_id = (
    SELECT e.categoria_id 
    FROM equipes e 
    WHERE e.id = atletas.equipe_id
) WHERE categoria_id IS NULL AND equipe_id IS NOT NULL;

-- Criar índices para performance se não existirem
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_atletas_categoria_id') THEN
        CREATE INDEX idx_atletas_categoria_id ON atletas(categoria_id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_atletas_evento_categoria') THEN
        CREATE INDEX idx_atletas_evento_categoria ON atletas(evento_id, categoria_id);
    END IF;
END
$$;