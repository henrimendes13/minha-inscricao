-- Dados iniciais para desenvolvimento
-- Compatible with UsuarioEntity consolidated structure

-- Inserir usuários de exemplo (Estrutura consolidada)
INSERT INTO usuarios (
    id, nome, email, senha, 
    aceita_termos, ativo, verificado, 
    -- Campos de atleta
    cpf, data_nascimento, genero, telefone, endereco,
    emergencia_nome, emergencia_telefone, observacoes_medicas,
    -- Campos de organizador
    nome_empresa, cnpj, descricao, site,
    created_at, updated_at
) VALUES 
-- Usuário 1: Administrador
(1, 'Admin Sistema', 'admin@eventsports.com.br', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 
 true, true, true, 
 null, null, null, null, null, null, null, null,
 null, null, null, null,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Usuário 2: Organizador (empresa)
(2, 'Carlos Organizador', 'organizador@test.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 
 true, true, true,
 null, null, null, '(11) 99999-0000', 'São Paulo, SP', null, null, null,
 'EventSports Brasil', '12.345.678/0001-90', 'Empresa organizadora de eventos esportivos', 'www.eventsports.com.br',
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Usuário 3: Atleta masculino
(3, 'João Silva', 'joao@example.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 
 true, true, false,
 '111.111.111-11', '1985-05-15', 'MASCULINO', '(11) 99999-1111', 'São Paulo, SP',
 'Ana Silva', '(11) 99999-1112', 'Nenhuma observação médica',
 null, null, null, null,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Usuário 4: Atleta feminina
(4, 'Maria Santos', 'maria@example.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 
 true, true, false,
 '222.222.222-22', '1992-08-20', 'FEMININO', '(11) 99999-2222', 'Rio de Janeiro, RJ',
 'Pedro Santos', '(11) 99999-2223', null,
 null, null, null, null,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Usuário 5: Atleta jovem
(5, 'Lucas Oliveira', 'lucas@example.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 
 true, true, false,
 '333.333.333-33', '2000-12-10', 'MASCULINO', '(11) 99999-3333', 'Belo Horizonte, MG',
 'Roberto Oliveira', '(11) 99999-3334', 'Alergia a medicamentos com ibuprofeno',
 null, null, null, null,
 CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Inserir eventos de exemplo (organizador_id referencia usuarios)
INSERT INTO eventos (
    id, nome, descricao, data_inicio_evento, data_fim_evento, 
    organizador_id, status, created_at, updated_at
) VALUES 
(1, 'CrossFit Games 2024', 
 'Competição de CrossFit para atletas de elite. Evento com múltiplas modalidades e categorias para todos os níveis.',
 '2024-12-15 08:00:00', '2024-12-17 18:00:00', 
 2, 'ABERTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'Maratona de São Paulo 2024', 
 'Maratona internacional de São Paulo com percurso de 42km pelas principais avenidas da cidade.',
 '2024-11-10 06:00:00', '2024-11-10 14:00:00', 
 2, 'ABERTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'Torneio de Natação Masters', 
 'Competição de natação para atletas masters em piscina olímpica com cronometragem eletrônica.',
 '2024-10-20 09:00:00', '2024-10-21 17:00:00', 
 2, 'ABERTO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Inserir categorias para os eventos
INSERT INTO categorias (
    id, nome, descricao, idade_minima, idade_maxima, genero,
    evento_id, ativa, tipo_participacao, quantidade_atletas_por_equipe, valor_inscricao, 
    created_at, updated_at
) VALUES 
-- Categorias do CrossFit Games
(1, 'RX Masculino', 'Categoria masculina RX para CrossFit - nível avançado', 18, 39, 'MASCULINO',
 1, true, 'INDIVIDUAL', 1, 150.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'RX Feminino', 'Categoria feminina RX para CrossFit - nível avançado', 18, 39, 'FEMININO',
 1, true, 'INDIVIDUAL', 1, 150.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'Scaled Masculino', 'Categoria masculina Scaled para iniciantes', 16, null, 'MASCULINO',
 1, true, 'INDIVIDUAL', 1, 100.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(4, 'Team Mixed', 'Categoria de equipes mistas (2H + 2M)', 18, null, null,
 1, true, 'EQUIPE', 4, 400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Categorias da Maratona
(5, 'Maratona Geral', 'Categoria geral da maratona 42km', 18, null, null,
 2, true, 'INDIVIDUAL', 1, 80.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(6, 'Maratona Elite', 'Categoria elite com tempo mínimo exigido', 18, 45, null,
 2, true, 'INDIVIDUAL', 1, 120.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Categorias da Natação
(7, 'Masters 25-29 Masculino', 'Natação Masters masculino 25-29 anos', 25, 29, 'MASCULINO',
 3, true, 'INDIVIDUAL', 1, 60.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(8, 'Masters 25-29 Feminino', 'Natação Masters feminino 25-29 anos', 25, 29, 'FEMININO',
 3, true, 'INDIVIDUAL', 1, 60.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(9, 'Masters 30-34 Geral', 'Natação Masters 30-34 anos - categoria mista', 30, 34, null,
 3, true, 'INDIVIDUAL', 1, 60.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Update sequences to current max values
SELECT setval('usuarios_id_seq', (SELECT COALESCE(MAX(id), 1) FROM usuarios));
SELECT setval('eventos_id_seq', (SELECT COALESCE(MAX(id), 1) FROM eventos));
SELECT setval('categorias_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categorias));