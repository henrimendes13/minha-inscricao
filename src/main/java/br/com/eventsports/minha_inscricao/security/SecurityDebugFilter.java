package br.com.eventsports.minha_inscricao.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Filtro de debug para Spring Security
 * Registra detalhes de cada request para ajudar a debugar problemas de autorização
 */
@Component
@Order(-100) // Executar antes dos filtros de segurança
@Slf4j
public class SecurityDebugFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        String requestId = generateRequestId();
        
        log.info("🔍 [{}] =============== INÍCIO DO REQUEST DEBUG ===============", requestId);
        logRequestDetails(request, requestId);
        
        try {
            // Executar o filtro chain
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("❌ [{}] EXCEÇÃO DURANTE O PROCESSAMENTO: {}", requestId, e.getMessage(), e);
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logResponseDetails(response, requestId, duration);
            log.info("🏁 [{}] =============== FIM DO REQUEST DEBUG ===============", requestId);
        }
    }

    private void logRequestDetails(HttpServletRequest request, String requestId) {
        log.info("📝 [{}] REQUEST DETAILS:", requestId);
        log.info("   Method: {}", request.getMethod());
        log.info("   URI: {}", request.getRequestURI());
        log.info("   URL: {}", request.getRequestURL());
        log.info("   Query String: {}", request.getQueryString());
        log.info("   Content Type: {}", request.getContentType());
        log.info("   Content Length: {}", request.getContentLength());
        log.info("   Remote Address: {}", request.getRemoteAddr());
        log.info("   Local Address: {}:{}", request.getLocalAddr(), request.getLocalPort());
        
        log.info("📋 [{}] REQUEST HEADERS:", requestId);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("   {}: {}", headerName, headerValue);
        }
        
        // Log do contexto de segurança antes do processamento
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🔐 [{}] SECURITY CONTEXT ANTES:", requestId);
        if (auth == null) {
            log.info("   Authentication: null");
        } else {
            log.info("   Authentication: {}", auth.getClass().getSimpleName());
            log.info("   Principal: {}", auth.getPrincipal());
            log.info("   Credentials: [HIDDEN]");
            log.info("   Authorities: {}", auth.getAuthorities());
            log.info("   Is Authenticated: {}", auth.isAuthenticated());
        }
    }

    private void logResponseDetails(HttpServletResponse response, String requestId, long duration) {
        log.info("📤 [{}] RESPONSE DETAILS:", requestId);
        log.info("   Status: {}", response.getStatus());
        log.info("   Content Type: {}", response.getContentType());
        log.info("   Duration: {}ms", duration);
        
        // Log do contexto de segurança após o processamento
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🔐 [{}] SECURITY CONTEXT DEPOIS:", requestId);
        if (auth == null) {
            log.info("   Authentication: null");
        } else {
            log.info("   Authentication: {}", auth.getClass().getSimpleName());
            log.info("   Principal: {}", auth.getPrincipal());
            log.info("   Credentials: [HIDDEN]");
            log.info("   Authorities: {}", auth.getAuthorities());
            log.info("   Is Authenticated: {}", auth.isAuthenticated());
        }
        
        // Log adicional para 403 errors
        if (response.getStatus() == 403) {
            log.error("🚫 [{}] 403 FORBIDDEN - REQUEST REJEITADO PELO SPRING SECURITY!", requestId);
            log.error("   Possíveis causas:");
            log.error("   - Endpoint não está configurado como permitAll()");
            log.error("   - CSRF proteção ativa (deveria estar disabled)");
            log.error("   - CORS preflight request falhando");
            log.error("   - @PreAuthorize annotation sem authentication");
            log.error("   - Authentication Manager tentando autenticar request público");
        }
    }

    private String generateRequestId() {
        return String.valueOf(System.currentTimeMillis() % 100000);
    }
}