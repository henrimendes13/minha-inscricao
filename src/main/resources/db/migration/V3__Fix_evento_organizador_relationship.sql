-- Migração para corrigir relacionamento organizador_id em eventos
-- Versão: V3
-- Descrição: Correção da FK organizador_id para referenciar usuarios ao invés de organizadores

-- Remover constraint existente
ALTER TABLE eventos DROP CONSTRAINT fkql8q9x9hq2qd0ltn3y8mhbp0y;

-- Recriar constraint correta
ALTER TABLE eventos ADD CONSTRAINT fkql8q9x9hq2qd0ltn3y8mhbp0y FOREIGN KEY (organizador_id) REFERENCES usuarios(id);

-- Atualizar dados existentes para referenciar usuarios ao invés de organizadores
UPDATE eventos SET organizador_id = 2 WHERE organizador_id = 1;