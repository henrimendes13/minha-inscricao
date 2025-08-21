-- Script de análise do banco de dados atual
-- Para executar via H2 Console

-- 1. Listar todas as tabelas
SELECT TABLE_NAME, TABLE_TYPE 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PUBLIC' 
ORDER BY TABLE_NAME;

-- 2. Examinar estrutura da tabela usuarios
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'USUARIOS'
ORDER BY ORDINAL_POSITION;

-- 3. Examinar estrutura da tabela atletas
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'ATLETAS'
ORDER BY ORDINAL_POSITION;

-- 4. Examinar estrutura da tabela organizadores
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'ORGANIZADORES'
ORDER BY ORDINAL_POSITION;

-- 5. Contar registros nas tabelas principais
SELECT 'usuarios' as tabela, COUNT(*) as total FROM usuarios
UNION ALL
SELECT 'atletas' as tabela, COUNT(*) as total FROM atletas
UNION ALL
SELECT 'organizadores' as tabela, COUNT(*) as total FROM organizadores
UNION ALL
SELECT 'eventos' as tabela, COUNT(*) as total FROM eventos
UNION ALL
SELECT 'inscricoes' as tabela, COUNT(*) as total FROM inscricoes;

-- 6. Examinar dados da tabela usuarios
SELECT id, email, nome, tipo, ativo, created_at 
FROM usuarios 
ORDER BY id;

-- 7. Examinar dados da tabela atletas (campos principais)
SELECT id, nome, cpf, data_nascimento, genero, telefone, evento_id, inscricao_id, created_at
FROM atletas 
ORDER BY id;

-- 8. Examinar dados da tabela organizadores
SELECT id, usuario_id, nome_empresa, cnpj, telefone, verificado, created_at
FROM organizadores 
ORDER BY id;

-- 9. Examinar relacionamentos - Eventos e seus organizadores
SELECT e.id as evento_id, e.nome as evento_nome, 
       o.id as organizador_id, o.nome_empresa, 
       u.nome as nome_usuario, u.email
FROM eventos e
LEFT JOIN organizadores o ON e.organizador_id = o.id
LEFT JOIN usuarios u ON o.usuario_id = u.id
ORDER BY e.id;

-- 10. Examinar relacionamentos - Atletas e suas inscrições
SELECT a.id as atleta_id, a.nome as atleta_nome, a.cpf,
       i.id as inscricao_id, i.status as status_inscricao,
       e.nome as evento_nome
FROM atletas a
LEFT JOIN inscricoes i ON a.inscricao_id = i.id
LEFT JOIN eventos e ON i.evento_id = e.id
ORDER BY a.id;