package br.com.eventsports.minha_inscricao.security;

import br.com.eventsports.minha_inscricao.service.Interfaces.IJwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticação JWT que intercepta todas as requisições HTTP
 * e verifica se contêm um token JWT válido no header Authorization.
 * 
 * Este filtro:
 * - Extrai o token JWT do header Authorization
 * - Valida o token usando o JwtService
 * - Carrega os dados do usuário usando o CustomUserDetailsService
 * - Configura o SecurityContext para permitir autorização via @PreAuthorize
 * 
 * Extends OncePerRequestFilter para garantir que seja executado apenas uma vez por requisição.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final IJwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Obter o header Authorization
        final String authHeader = request.getHeader("Authorization");
        
        // Verificar se o header existe e tem o formato correto (Bearer token)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Requisição sem token JWT válido - URI: {} - Method: {} - AuthHeader: {}", 
                    request.getRequestURI(), request.getMethod(), authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extrair o token (remover "Bearer " prefix)
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);
            
            log.debug("Token JWT encontrado para usuário: {} - URI: {}", 
                    userEmail, request.getRequestURI());

            // Se temos um username e ainda não há autenticação no contexto
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Carregar os detalhes do usuário
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
                // Validar o token
                if (jwtService.validateToken(jwt, userDetails)) {
                    
                    // Criar token de autenticação do Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Adicionar detalhes da requisição
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Configurar o contexto de segurança
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Usuário autenticado com sucesso: {} - Authorities: {}", 
                            userEmail, userDetails.getAuthorities());
                    
                } else {
                    log.warn("Token JWT inválido para usuário: {}", userEmail);
                }
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar token JWT - URI: {} - Erro: {}", 
                    request.getRequestURI(), e.getMessage());
            
            // Limpar o contexto de segurança em caso de erro
            SecurityContextHolder.clearContext();
        }

        // Continuar com a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Determina se este filtro deve ser aplicado à requisição.
     * Pula endpoints públicos para melhor performance.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Não filtrar endpoints públicos específicos (exceto /api/auth/me que precisa de autenticação)
        boolean shouldSkip = (path.startsWith("/api/auth/") && !path.equals("/api/auth/me")) ||
               path.startsWith("/api/usuarios") && "POST".equals(method) ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/h2-console/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/webjars/") ||
               "OPTIONS".equals(method);
        
        log.debug("shouldNotFilter - URI: {} - Method: {} - Skip: {}", path, method, shouldSkip);
        return shouldSkip;
    }
}