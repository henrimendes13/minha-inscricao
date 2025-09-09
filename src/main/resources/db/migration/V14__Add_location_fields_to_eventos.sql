-- Migração para adicionar campos de localização à tabela eventos
-- Versão: V14
-- Descrição: Adiciona campos cidade, estado e endereco à tabela eventos

-- Adicionar colunas de localização à tabela eventos
ALTER TABLE eventos ADD COLUMN cidade VARCHAR(100);
ALTER TABLE eventos ADD COLUMN estado VARCHAR(50);
ALTER TABLE eventos ADD COLUMN endereco VARCHAR(300);