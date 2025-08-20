package br.com.eventsports.minha_inscricao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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

/**
 * TEMPORARY Security Configuration for debugging 403 errors.
 * This configuration permits ALL endpoints to isolate security issues.
 * 
 * Usage: Add -Dspring.profiles.active=debug to JVM arguments or set SPRING_PROFILES_ACTIVE=debug
 * 
 * WARNING: DO NOT USE IN PRODUCTION - THIS DISABLES ALL SECURITY
 */
@Configuration
@EnableWebSecurity
@Profile("debug")
public class DebugSecurityConfig {

    @Bean
    public SecurityFilterChain debugFilterChain(HttpSecurity http) throws Exception {
        System.out.println("🔓 DEBUG SECURITY CONFIG ACTIVE - ALL ENDPOINTS PERMITTED!");
        
        http
            // Enhanced CORS configuration
            .cors(cors -> cors.configurationSource(debugCorsConfigurationSource()))
            
            // Disable CSRF completely
            .csrf(csrf -> csrf.disable())
            
            // PERMIT ALL ENDPOINTS - NO AUTHENTICATION REQUIRED
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            )
            
            // Stateless session
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Permissive headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig.disable())
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource debugCorsConfigurationSource() {
        System.out.println("🌐 DEBUG CORS CONFIG - PERMITTING ALL ORIGINS AND METHODS");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow ALL origins
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedOrigins(Arrays.asList("*"));
        
        // Allow ALL methods including OPTIONS
        configuration.setAllowedMethods(Arrays.asList("*"));
        
        // Allow ALL headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Expose all headers
        configuration.setExposedHeaders(Arrays.asList("*"));
        
        // Set max age for preflight cache (1 hour)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to ALL paths
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    public PasswordEncoder debugPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}