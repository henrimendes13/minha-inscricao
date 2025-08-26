-- Permitir que atleta_id seja NULL para inscrições de equipe
ALTER TABLE inscricoes 
ALTER COLUMN atleta_id DROP NOT NULL;