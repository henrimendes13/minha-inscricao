package br.com.eventsports.minha_inscricao.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança MINIMAL para debug
 * 
 * Para usar esta configuração ao invés da principal:
 * 1. Adicione no application.properties: security.config.minimal=true
 * 2. Renomeie SecurityConfig.java para SecurityConfig.java.bak
 * 3. Reinicie a aplicação
 * 
 * Esta configuração é APENAS para debug - NÃO usar em produção!
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "security.config.minimal", havingValue = "true")
@Order(0) // Prioridade máxima
@Slf4j
public class SecurityConfigMinimal {

    @Bean
    public SecurityFilterChain minimalFilterChain(HttpSecurity http) throws Exception {
        log.warn("🚨 USANDO CONFIGURAÇÃO DE SEGURANÇA MINIMAL - APENAS PARA DEBUG!");
        log.warn("🚨 NÃO USAR EM PRODUÇÃO!");
        
        http
            .csrf(csrf -> {
                log.info("🚫 Desabilitando CSRF");
                csrf.disable();
            })
            .cors(cors -> {
                log.info("🌐 Desabilitando CORS checks");
                cors.disable();
            })
            .headers(headers -> {
                log.info("🔧 Configurando headers minimos");
                headers
                    .frameOptions(frame -> frame.disable())
                    .httpStrictTransportSecurity(hsts -> hsts.disable());
            })
            .sessionManagement(session -> {
                log.info("📝 Session management: STATELESS");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authorizeHttpRequests(authz -> {
                log.info("🔓 PERMITINDO TODOS OS REQUESTS - CONFIGURAÇÃO MINIMAL");
                authz.anyRequest().permitAll();
            })
            .httpBasic(basic -> {
                log.info("🚫 Desabilitando HTTP Basic");
                basic.disable();
            })
            .formLogin(form -> {
                log.info("🚫 Desabilitando Form Login");
                form.disable();
            })
            .logout(logout -> {
                log.info("🚫 Desabilitando Logout");
                logout.disable();
            });
        
        log.warn("🚨 CONFIGURAÇÃO MINIMAL ATIVA - TODOS OS ENDPOINTS PÚBLICOS!");
        return http.build();
    }
}