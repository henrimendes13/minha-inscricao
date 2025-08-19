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

    private static final String SECURITY_SCHEME_NAME = "sessionAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento de Inscrições para Competições")
                        .description("API REST para gerenciar eventos esportivos e suas inscrições. " +
                                "Permite criar, consultar, atualizar e deletar eventos com cache em memória.\n\n" +
                                "**AUTENTICAÇÃO:**\n" +
                                "1. Use o endpoint POST /api/auth/login para fazer login\n" +
                                "2. O cookie de sessão JSESSIONID será definido automaticamente\n" +
                                "3. Para endpoints protegidos, clique no botão 'Authorize' e insira 'JSESSIONID=valor_do_cookie'\n" +
                                "4. Ou deixe vazio se já fez login - o browser incluirá automaticamente o cookie\n" +
                                "5. Use POST /api/auth/logout para encerrar a sessão\n" +
                                "6. Use GET /api/auth/me para verificar dados do usuário logado")
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
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Cookie de sessão HTTP. Obtido automaticamente após login via /api/auth/login")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}
