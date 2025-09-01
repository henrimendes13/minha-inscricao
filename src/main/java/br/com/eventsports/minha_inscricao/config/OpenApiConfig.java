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


    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento de Inscrições para Competições")
                        .description("API REST para gerenciar eventos esportivos e suas inscrições. " +
                                "Permite criar, consultar, atualizar e deletar eventos com cache em memória.\n\n" +
                                "**Autenticação:** Use o botão 'Authorize' para fazer login como admin:\n" +
                                "- Email: admin@admin.com\n" +
                                "- Senha: admin\n\n" +
                                "Endpoints públicos não precisam de autenticação. " +
                                "Endpoints com 🔒 precisam de token JWT.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("EventSports Team")
                                .email("contato@eventsports.com.br")
                                .url("https://eventsports.com.br"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                
                // Configuração de Segurança JWT
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT obtido através do login admin. " +
                                           "Não inclua a palavra 'Bearer', apenas o token.")))
                
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.eventsports.com.br")
                                .description("Servidor de Produção (exemplo)")
                ));
    }
}
