package br.com.eventsports.minha_inscricao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;

/**
 * CORS Debug Configuration with detailed logging for troubleshooting 403 errors.
 * This configuration provides enhanced CORS handling with verbose logging.
 * 
 * Usage: Add -Dspring.profiles.active=cors-debug to JVM arguments
 */
@Configuration
@Profile("cors-debug")
public class CorsDebugConfig {

    @Bean
    public CorsFilter corsDebugFilter() {
        System.out.println("🔍 CORS DEBUG FILTER INITIALIZED");
        
        return new CorsFilter(corsDebugConfigurationSource()) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                    FilterChain filterChain) throws ServletException, IOException {
                
                String method = request.getMethod();
                String origin = request.getHeader("Origin");
                String requestURI = request.getRequestURI();
                
                System.out.println("🌐 CORS DEBUG - Method: " + method + 
                                 ", URI: " + requestURI + 
                                 ", Origin: " + origin);
                
                // Log all relevant headers
                System.out.println("🔍 Headers:");
                System.out.println("  - Access-Control-Request-Method: " + 
                                 request.getHeader("Access-Control-Request-Method"));
                System.out.println("  - Access-Control-Request-Headers: " + 
                                 request.getHeader("Access-Control-Request-Headers"));
                System.out.println("  - Content-Type: " + request.getHeader("Content-Type"));
                
                // Handle OPTIONS preflight specifically
                if ("OPTIONS".equalsIgnoreCase(method)) {
                    System.out.println("✈️ Handling OPTIONS preflight request for: " + requestURI);
                    
                    // Set CORS headers explicitly for OPTIONS
                    response.setHeader("Access-Control-Allow-Origin", origin != null ? origin : "*");
                    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                    response.setHeader("Access-Control-Allow-Headers", 
                        "Origin, X-Requested-With, Content-Type, Accept, Authorization, Cache-Control, Pragma");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setHeader("Access-Control-Max-Age", "3600");
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    System.out.println("✅ OPTIONS response sent with status 200");
                    return; // Don't continue the chain for OPTIONS
                }
                
                System.out.println("➡️ Continuing filter chain for: " + method + " " + requestURI);
                super.doFilterInternal(request, response, filterChain);
                System.out.println("✅ Filter chain completed for: " + method + " " + requestURI);
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsDebugConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Most permissive settings for debugging
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);
        
        // Apply to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("🔧 CORS Debug Configuration Source created with permissive settings");
        return source;
    }
}