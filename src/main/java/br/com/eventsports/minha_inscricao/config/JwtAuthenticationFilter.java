package br.com.eventsports.minha_inscricao.config;

import br.com.eventsports.minha_inscricao.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = getTokenFromRequest(request);
            
            if (token != null && jwtUtil.validateToken(token)) {
                // Verifica se é token de ADMIN antes de autenticar
                if (jwtUtil.isAdminToken(token)) {
                    Authentication authentication = jwtUtil.getAuthenticationFromToken(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Usuário ADMIN autenticado: {}", authentication.getName());
                } else {
                    log.warn("Tentativa de uso de token não-ADMIN em endpoint protegido");
                }
            }
        } catch (Exception e) {
            log.error("Erro ao processar token JWT: {}", e.getMessage());
            // Limpa o contexto de segurança em caso de erro
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do header Authorization
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * Não aplica o filtro em rotas públicas para melhor performance
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Não aplica filtro em rotas públicas (para performance)
        return path.startsWith("/api/auth/admin/login") ||
               path.startsWith("/api/eventos") ||
               path.startsWith("/api/categorias") ||
               path.startsWith("/api/atletas") ||
               path.startsWith("/api/equipes") ||
               path.startsWith("/api/inscricoes") ||
               path.startsWith("/api/usuarios") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/favicon.ico");
    }
}