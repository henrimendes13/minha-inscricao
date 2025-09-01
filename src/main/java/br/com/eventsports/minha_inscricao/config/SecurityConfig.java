package br.com.eventsports.minha_inscricao.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita configurações de segurança stateful
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Configuração stateless para JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configuração de autorização
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de login públicos
                        .requestMatchers("/api/auth/admin/login").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers(POST, "/api/usuarios").permitAll()

                        // Endpoints que exigem autenticação ADMIN
                        .requestMatchers("/api/auth/admin/**").hasAuthority("ADMIN")

                        // Endpoints de Eventos - Permissões básicas por ROLE
                        .requestMatchers(POST, "/api/eventos").hasAnyAuthority("ADMIN", "ORGANIZADOR", "ATLETA")
                        .requestMatchers(PUT, "/api/eventos/**").hasAnyAuthority("ADMIN", "ORGANIZADOR")
                        .requestMatchers(DELETE, "/api/eventos/**").hasAnyAuthority("ADMIN", "ORGANIZADOR")

                        // Endpoints de Categorias vinculadas a eventos
                        .requestMatchers(POST, "/api/categorias").hasAnyAuthority("ADMIN", "ORGANIZADOR")
                        .requestMatchers(PUT, "/api/categorias/**").hasAnyAuthority("ADMIN", "ORGANIZADOR")
                        .requestMatchers(DELETE, "/api/categorias/**").hasAnyAuthority("ADMIN", "ORGANIZADOR")

                        // Endpoints de Workouts vinculados a eventos
                        .requestMatchers(POST, "/api/workouts").hasAnyAuthority("ADMIN", "ORGANIZADOR")
                        .requestMatchers(PUT, "/api/workouts/**").hasAnyAuthority("ADMIN", "ORGANIZADOR")
                        .requestMatchers(DELETE, "/api/workouts/**").hasAnyAuthority("ADMIN", "ORGANIZADOR")

                        // Swagger/OpenAPI (público para desenvolvimento)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // Endpoints públicos de leitura
                        .requestMatchers(GET, "/api/eventos/**").permitAll()
                        .requestMatchers(GET, "/api/categorias/**").permitAll()
                        .requestMatchers(GET, "/api/usuarios/**").hasAuthority("ADMIN")

                        // Todos os outros endpoints ficam públicos por enquanto
                        // (Outros tipos de autenticação ainda não implementados)
                        .anyRequest().permitAll())

                // Adiciona filtro JWT antes do filtro padrão de autenticação
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}