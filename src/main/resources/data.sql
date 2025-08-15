-- Dados iniciais para a tabela eventos
INSERT INTO eventos (nome, data, descricao, created_at, updated_at) VALUES
('CrossFit Games 2024', '2024-08-01 10:00:00', 
'Competição de CrossFit com atletas de elite. Evento principal do ano com premiação de R$ 10.000,00', 
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Maratona de São Paulo', '2024-06-15 06:00:00', 
'42ª Maratona Internacional de São Paulo. Percurso tradicional com largada no Ibirapuera', 
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('Torneio de Natação Masters', '2024-07-20 08:00:00', 
'Competição de natação para atletas masters (35+ anos). Piscina semi-olímpica aquecida', 
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
