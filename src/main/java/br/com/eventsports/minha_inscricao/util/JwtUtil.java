package br.com.eventsports.minha_inscricao.util;

import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret:minha-inscricao-admin-secret-key-super-segura-para-jwt}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 horas
    private long jwtExpiration;

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Gera token JWT para o usuário admin especial (admin@admin.com)
     */
    public String generateAdminToken(UsuarioEntity usuario) {
        if (!"admin@admin.com".equals(usuario.getEmail())) {
            throw new SecurityException("Token ADMIN JWT apenas pode ser gerado para admin@admin.com");
        }

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("tipoUsuario", "ADMIN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Gera token JWT para qualquer tipo de usuário baseado em seu tipo real
     */
    public String generateToken(UsuarioEntity usuario) {
        if (usuario == null || usuario.getEmail() == null) {
            throw new SecurityException("Usuário inválido para geração de token");
        }
        
        // Caso especial: admin@admin.com sempre é ADMIN
        if ("admin@admin.com".equals(usuario.getEmail())) {
            return generateAdminToken(usuario);
        }
        
        // Para outros usuários, usar o tipo determinado pelo sistema
        TipoUsuario tipoUsuario = usuario.getTipoUsuario();
        String role = "ROLE_" + tipoUsuario.name();
        
        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("tipoUsuario", tipoUsuario.name())
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Valida se o token é válido
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT não suportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Falha na validação da assinatura do token JWT: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vazio ou inválido: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extrai o email do token
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * Extrai o ID do usuário do token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * Extrai o nome do usuário do token
     */
    public String getNomeFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("nome", String.class);
    }

    /**
     * Verifica se o token é de um usuário ADMIN
     */
    public boolean isAdminToken(String token) {
        try {
            Claims claims = getClaims(token);
            String tipoUsuario = claims.get("tipoUsuario", String.class);
            return TipoUsuario.ADMIN.name().equals(tipoUsuario);
        } catch (Exception e) {
            log.warn("Erro ao verificar se token é de ADMIN: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extrai o tipo de usuário do token
     */
    public String getTipoUsuarioFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("tipoUsuario", String.class);
        } catch (Exception e) {
            log.warn("Erro ao extrair tipo de usuário do token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extrai a role do token
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            log.warn("Erro ao extrair role do token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cria objeto Authentication para o Spring Security
     */
    public Authentication getAuthenticationFromToken(String token) {
        if (!isAdminToken(token)) {
            throw new SecurityException("Token não é de usuário ADMIN");
        }

        String email = getEmailFromToken(token);
        return new UsernamePasswordAuthenticationToken(
                email, 
                null, 
                Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    /**
     * Verifica se o token está expirado
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extrai claims do token
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}