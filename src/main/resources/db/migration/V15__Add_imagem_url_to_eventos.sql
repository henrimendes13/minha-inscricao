-- Migração para adicionar campo de imagem URL à tabela eventos
-- Versão: V15
-- Descrição: Adiciona campo imagem_url à tabela eventos para armazenar imagens dos eventos

-- Adicionar coluna de imagem URL à tabela eventos
ALTER TABLE eventos ADD COLUMN imagem_url VARCHAR(500);