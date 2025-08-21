package br.com.eventsports.minha_inscricao.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/database-analysis")
@RequiredArgsConstructor
public class DatabaseAnalysisController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/tables")
    public ResponseEntity<List<Map<String, Object>>> getTables() {
        try {
            String sql = """
                SELECT TABLE_NAME, TABLE_TYPE 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_SCHEMA = 'PUBLIC' 
                ORDER BY TABLE_NAME
                """;
            
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/count-records")
    public ResponseEntity<Map<String, Object>> getRecordCounts() {
        try {
            Map<String, Object> counts = new HashMap<>();
            
            // Contar registros das tabelas principais
            String[] tables = {"usuarios", "atletas", "organizadores", "eventos", "inscricoes", "categorias"};
            
            for (String table : tables) {
                try {
                    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
                    counts.put(table, count != null ? count : 0);
                } catch (Exception e) {
                    counts.put(table, "ERROR: " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<Map<String, Object>>> getUsuarios() {
        try {
            String sql = """
                SELECT id, email, nome, tipo, ativo, created_at 
                FROM usuarios 
                ORDER BY id
                """;
            
            List<Map<String, Object>> usuarios = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/atletas")
    public ResponseEntity<List<Map<String, Object>>> getAtletas() {
        try {
            String sql = """
                SELECT id, nome, cpf, data_nascimento, genero, telefone, 
                       evento_id, inscricao_id, created_at
                FROM atletas 
                ORDER BY id
                """;
            
            List<Map<String, Object>> atletas = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(atletas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/organizadores")
    public ResponseEntity<List<Map<String, Object>>> getOrganizadores() {
        try {
            String sql = """
                SELECT id, usuario_id, nome_empresa, cnpj, telefone, 
                       verificado, created_at
                FROM organizadores 
                ORDER BY id
                """;
            
            List<Map<String, Object>> organizadores = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(organizadores);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/eventos-organizadores")
    public ResponseEntity<List<Map<String, Object>>> getEventosOrganizadores() {
        try {
            String sql = """
                SELECT e.id as evento_id, e.nome as evento_nome, 
                       o.id as organizador_id, o.nome_empresa, 
                       u.nome as nome_usuario, u.email
                FROM eventos e
                LEFT JOIN organizadores o ON e.organizador_id = o.id
                LEFT JOIN usuarios u ON o.usuario_id = u.id
                ORDER BY e.id
                """;
            
            List<Map<String, Object>> resultado = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/atletas-inscricoes")
    public ResponseEntity<List<Map<String, Object>>> getAtletasInscricoes() {
        try {
            String sql = """
                SELECT a.id as atleta_id, a.nome as atleta_nome, a.cpf,
                       i.id as inscricao_id, i.status as status_inscricao,
                       e.nome as evento_nome
                FROM atletas a
                LEFT JOIN inscricoes i ON a.inscricao_id = i.id
                LEFT JOIN eventos e ON i.evento_id = e.id
                ORDER BY a.id
                """;
            
            List<Map<String, Object>> resultado = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @GetMapping("/schema/{table}")
    public ResponseEntity<List<Map<String, Object>>> getTableSchema(@PathVariable String table) {
        try {
            String sql = """
                SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;
            
            List<Map<String, Object>> schema = jdbcTemplate.queryForList(sql, table.toUpperCase());
            return ResponseEntity.ok(schema);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(List.of(Map.of("error", e.getMessage())));
        }
    }

    @PostMapping("/load-initial-data")
    public ResponseEntity<Map<String, Object>> loadInitialData() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Inserir usuários de exemplo
            String usuariosSql = """
                INSERT INTO usuarios (id, nome, email, senha, tipo, ativo, created_at, updated_at) VALUES
                (1, 'Admin Sistema', 'admin@admin.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (2, 'Organizador Teste', 'organizador@test.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ORGANIZADOR', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (3, 'João Silva', 'joao@example.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ATLETA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (4, 'Maria Santos', 'maria@example.com', '$2a$10$N.WphQbp6rspAZN4Fq1qhO5M5LDm7LjLLFJf7nKnhZ.ckJ3LqPKem', 'ATLETA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
            
            jdbcTemplate.execute(usuariosSql);
            
            // Inserir organizadores
            String organizadoresSql = """
                INSERT INTO organizadores (id, usuario_id, nome_empresa, cnpj, telefone, endereco, descricao, site, verificado, created_at, updated_at) VALUES
                (1, 2, 'EventSports Brasil', '12.345.678/0001-90', '(11) 99999-0000', 'São Paulo, SP', 'Empresa organizadora de eventos esportivos', 'www.eventsports.com.br', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
            
            jdbcTemplate.execute(organizadoresSql);
            
            // Inserir atletas
            String atletasSql = """
                INSERT INTO atletas (id, nome, cpf, data_nascimento, genero, telefone, endereco, aceita_termos, created_at, updated_at) VALUES
                (1, 'João Silva', '111.111.111-11', '1985-05-15', 'MASCULINO', '(11) 99999-1111', 'São Paulo, SP', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
                (2, 'Maria Santos', '222.222.222-22', '1992-08-20', 'FEMININO', '(11) 99999-2222', 'Rio de Janeiro, RJ', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """;
            
            jdbcTemplate.execute(atletasSql);
            
            // Inserir eventos
            String eventosSql = """
                INSERT INTO eventos (id, nome, descricao, data_inicio_evento, data_fim_evento, organizador_id, created_at, updated_at, status) VALUES
                (1, 'CrossFit Games 2024', 'Competição de CrossFit para atletas de elite', '2024-08-08 08:00:00', '2024-08-11 18:00:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ABERTO'),
                (2, 'Maratona de São Paulo 2024', 'Maratona internacional de São Paulo', '2024-05-12 06:00:00', '2024-05-12 14:00:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ABERTO'),
                (3, 'Torneio de Natação Masters', 'Competição de natação para masters', '2024-06-15 09:00:00', '2024-06-16 17:00:00', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ABERTO')
                """;
            
            jdbcTemplate.execute(eventosSql);
            
            // Verificar contagens após inserção
            String[] tables = {"usuarios", "atletas", "organizadores", "eventos"};
            for (String table : tables) {
                Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
                result.put(table + "_count", count != null ? count : 0);
            }
            
            result.put("status", "success");
            result.put("message", "Dados iniciais carregados com sucesso!");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage(), "status", "error"));
        }
    }
}