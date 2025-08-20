-- Dados iniciais para desenvolvimento
-- Este arquivo é carregado apenas quando o banco está vazio

-- Inserir usuários de exemplo
INSERT INTO usuarios (id, nome, email, senha, tipo, ativo, created_at, updated_at) VALUES
(1, 'Admin Sistema', 'admin@admin.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Organizador Teste', 'organizador@test.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ORGANIZADOR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'João Silva', 'joao@example.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ATLETA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Maria Santos', 'maria@example.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ATLETA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserir organizadores
INSERT INTO organizadores (id, usuario_id, nome_empresa, created_at, updated_at, verificado) VALUES
(1, 2, 'EventSports Brasil', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

-- Inserir atletas
INSERT INTO atletas (id, nome, cpf, data_nascimento, genero, telefone, endereco, aceita_termos, created_at, updated_at) VALUES
(1, 'João Silva', '111.111.111-11', '1985-05-15', 'MASCULINO', '(11) 99999-1111', 'São Paulo, SP', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Maria Santos', '222.222.222-22', '1992-08-20', 'FEMININO', '(11) 99999-2222', 'Rio de Janeiro, RJ', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserir eventos de exemplo
INSERT INTO eventos (id, nome, descricao, data_inicio_evento, data_fim_evento, organizador_id, created_at, updated_at, status) VALUES
(1, 'CrossFit Games 2024', 'Competição de CrossFit para atletas de elite', '2024-08-08 08:00:00', '2024-08-11 18:00:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ABERTO'),
(2, 'Maratona de São Paulo 2024', 'Maratona internacional de São Paulo', '2024-05-12 06:00:00', '2024-05-12 14:00:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ABERTO'),
(3, 'Torneio de Natação Masters', 'Competição de natação para masters', '2024-06-15 09:00:00', '2024-06-16 17:00:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ABERTO');

-- Inserir categorias
INSERT INTO categorias (id, nome, descricao, idade_minima, idade_maxima, evento_id, created_at, updated_at, ativa, tipo_participacao, valor_inscricao) VALUES
(1, 'RX Masculino', 'Categoria masculina RX para CrossFit', 18, 39, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'INDIVIDUAL', 100.00),
(2, 'RX Feminino', 'Categoria feminina RX para CrossFit', 18, 39, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'INDIVIDUAL', 100.00),
(3, 'Maratona Geral', 'Categoria geral da maratona', 18, null, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'INDIVIDUAL', 50.00),
(4, 'Masters 25-29', 'Natação Masters 25-29 anos', 25, 29, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'INDIVIDUAL', 75.00);