package br.com.eventsports.minha_inscricao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Configuração CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Desabilitar CSRF para facilitar desenvolvimento
            .csrf(csrf -> csrf.disable())
            
            // Configuração de autorização
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos para eventos (GET apenas)
                .requestMatchers("GET", "/api/eventos/**").permitAll()
                
                // Endpoints para registro de usuários
                .requestMatchers("POST", "/api/usuarios").permitAll()
                .requestMatchers("POST", "/api/usuarios/com-organizador").permitAll()
                
                // Endpoints públicos para login/auth
                .requestMatchers("/api/auth/**").permitAll()
                
                // Documentação e monitoramento
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/cache").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()
                
                // H2 Console para desenvolvimento
                .requestMatchers("/h2-console/**").permitAll()
                
                // Recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                
                // Todos os outros endpoints precisam de autenticação
                .anyRequest().authenticated()
            )
            
            // Configuração de session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().changeSessionId() // Proteção contra session fixation
                .maximumSessions(1) // Máximo de 1 sessão por usuário
                .sessionRegistry(sessionRegistry())
            )
            
            // Headers de segurança
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Permitir H2 Console em iframe
                .contentTypeOptions(contentTypeOptions -> {}) // Ativar proteção de Content Type
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
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

    @Bean
    public UserDetailsService userDetailsService() {
        // UserDetailsService básico para desenvolvimento
        // Em produção, isso deve ser substituído por implementação customizada
        UserDetails user = User.builder()
                .username("admin@test.com")
                .password(passwordEncoder().encode("123456"))
                .roles("ADMIN")
                .build();
        
        UserDetails user2 = User.builder()
                .username("atleta@test.com")
                .password(passwordEncoder().encode("123456"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user, user2);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}