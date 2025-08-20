package br.com.eventsports.minha_inscricao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        initializeDataIfEmpty();
    }

    private void initializeDataIfEmpty() {
        try {
            // Verifica se existem dados na tabela eventos
            Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eventos", 
                Long.class
            );
            
            if (count == null || count == 0) {
                log.info("Banco de dados vazio. Inicializando dados de exemplo...");
                loadInitialData();
            } else {
                log.info("Banco de dados já possui {} eventos. Pulando inicialização.", count);
            }
            
        } catch (Exception e) {
            log.warn("Não foi possível verificar dados existentes. Tabelas podem não ter sido criadas ainda: {}", 
                    e.getMessage());
            // Se as tabelas não existem ainda, não fazer nada
            // O Hibernate criará as tabelas e depois este método pode ser executado novamente
        }
    }

    private void loadInitialData() {
        try {
            // Carrega o arquivo data.sql do classpath se existir e tiver conteúdo
            ClassPathResource resource = new ClassPathResource("initial-data.sql");
            
            if (resource.exists()) {
                String sql = StreamUtils.copyToString(
                    resource.getInputStream(), 
                    StandardCharsets.UTF_8
                );
                
                if (!sql.trim().isEmpty() && !sql.contains("-- Os dados de exemplo foram removidos")) {
                    // Divide o SQL em statements individuais e executa cada um
                    String[] statements = sql.split(";");
                    for (String statement : statements) {
                        String cleanStatement = statement.trim();
                        if (!cleanStatement.isEmpty() && !cleanStatement.startsWith("--")) {
                            jdbcTemplate.execute(cleanStatement);
                        }
                    }
                    log.info("Dados iniciais carregados com sucesso!");
                } else {
                    log.info("Arquivo de dados iniciais vazio ou contém apenas comentários.");
                    createBasicExampleData();
                }
            } else {
                log.info("Arquivo initial-data.sql não encontrado. Criando dados básicos de exemplo...");
                createBasicExampleData();
            }
            
        } catch (Exception e) {
            log.error("Erro ao carregar dados iniciais: {}", e.getMessage(), e);
        }
    }

    private void createBasicExampleData() {
        log.info("Criando dados básicos de exemplo via JdbcTemplate...");
        // Aqui você pode criar dados básicos programaticamente se necessário
        // Por enquanto, apenas log informativo
    }
}