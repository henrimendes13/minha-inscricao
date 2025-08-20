package br.com.eventsports.minha_inscricao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuração CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Desabilitar CSRF para facilitar desenvolvimento
            .csrf(csrf -> csrf.disable())
            
            // Configuração de autorização - MUITO ESPECÍFICA E ORDENADA
            .authorizeHttpRequests(authz -> authz
                // PRIMEIRO: Endpoints públicos específicos para criação de usuários
                .requestMatchers("POST", "/api/usuarios").permitAll()
                .requestMatchers("POST", "/api/usuarios/com-organizador").permitAll()
                
                // Endpoints públicos de autenticação
                .requestMatchers("/api/auth/**").permitAll()
                
                // Endpoints públicos para consulta de eventos
                .requestMatchers("GET", "/api/eventos/**").permitAll()
                
                // Endpoints de documentação e monitoramento
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // H2 Console para desenvolvimento
                .requestMatchers("/h2-console/**").permitAll()
                
                // Recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                
                // OPTIONS requests para CORS preflight
                .requestMatchers("OPTIONS", "/**").permitAll()
                
                // TODOS os outros endpoints requerem autenticação
                .anyRequest().authenticated()
            )
            
            // Configuração de session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Headers de segurança
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Permitir H2 Console em iframe
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir origens específicas ou todas para desenvolvimento
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Permitir credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Headers expostos ao client
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-Total-Count"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}