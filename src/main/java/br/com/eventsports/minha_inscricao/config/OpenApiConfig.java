package br.com.eventsports.minha_inscricao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento de Inscrições para Competições")
                        .description("API REST para gerenciar eventos esportivos e suas inscrições. " +
                                "Permite criar, consultar, atualizar e deletar eventos com cache em memória.\n\n" +
                                "**AUTENTICAÇÃO JWT:**\n" +
                                "1. Use o endpoint POST /api/auth/login para fazer login e obter token JWT\n" +
                                "2. Copie o token retornado no campo 'token' da resposta\n" +
                                "3. Para endpoints protegidos, clique no botão 'Authorize' e insira o token JWT\n" +
                                "4. O token deve ser inserido SEM o prefixo 'Bearer ' - apenas o token\n" +
                                "5. Use POST /api/auth/logout para invalidar o token (lado cliente)\n" +
                                "6. Use GET /api/auth/me para verificar dados do usuário autenticado\n" +
                                "7. Tokens expiram em 24 horas - faça novo login se necessário")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("EventSports Team")
                                .email("contato@eventsports.com.br")
                                .url("https://eventsports.com.br"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.eventsports.com.br")
                                .description("Servidor de Produção (exemplo)")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT. Obtido via POST /api/auth/login e usado no header Authorization: Bearer <token>")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}
