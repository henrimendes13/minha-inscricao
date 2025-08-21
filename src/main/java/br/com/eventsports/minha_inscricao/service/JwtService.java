package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.exception.JwtAuthenticationException;
import br.com.eventsports.minha_inscricao.security.CustomUserPrincipal;
import br.com.eventsports.minha_inscricao.service.Interfaces.IJwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementação do serviço JWT para gerenciamento de tokens de autenticação.
 * 
 * Este serviço utiliza a biblioteca JJWT para:
 * - Criar tokens JWT assinados com HS256
 * - Validar integridade e expiração de tokens
 * - Extrair informações (claims) dos tokens
 * - Incluir roles/authorities do usuário nos tokens
 * 
 * Configurações:
 * - Secret Key: Configurada via application.properties (jwt.secret)
 * - Expiração: Configurada via application.properties (jwt.expiration)
 * - Algoritmo: HMAC SHA-256 (HS256)
 */
@Service
@Slf4j
public class JwtService implements IJwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Gera um token JWT para o usuário fornecido.
     * Automaticamente inclui as roles/authorities do usuário como claims.
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        log.debug("Gerando token JWT para usuário: {}", userDetails.getUsername());
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Gera um token JWT com claims customizadas adicionais.
     */
    @Override
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        log.debug("Gerando token JWT com claims extras para usuário: {}", userDetails.getUsername());
        
        // Adiciona as authorities/roles do usuário às claims
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        
        extraClaims.put("authorities", authorities);
        
        // Se o UserDetails é um CustomUserPrincipal, adicionar o ID do usuário
        if (userDetails instanceof CustomUserPrincipal customPrincipal) {
            extraClaims.put("userId", customPrincipal.getId());
            log.debug("ID do usuário incluído no token: {}", customPrincipal.getId());
        }
        
        log.debug("Authorities incluídas no token: {}", authorities);
        
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Valida um token JWT verificando integridade e expiração.
     */
    @Override
    public Boolean validateToken(String token, UserDetails userDetails) {
        log.debug("Validando token JWT para usuário: {}", userDetails.getUsername());
        
        try {
            final String username = extractUsername(token);
            boolean isValidUser = username.equals(userDetails.getUsername());
            boolean isNotExpired = !isTokenExpired(token);
            
            boolean isValid = isValidUser && isNotExpired;
            
            if (isValid) {
                log.debug("Token válido para usuário: {}", username);
            } else {
                log.warn("Token inválido para usuário: {} (valid user: {}, not expired: {})", 
                        username, isValidUser, isNotExpired);
            }
            
            return isValid;
            
        } catch (JwtException e) {
            log.error("Erro ao validar token JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o username (subject) do token JWT.
     */
    @Override
    public String extractUsername(String token) {
        log.debug("Extraindo username do token JWT");
        String username = extractClaim(token, Claims::getSubject);
        log.debug("Username extraído: {}", username);
        return username;
    }

    /**
     * Extrai uma claim específica do token usando um resolver.
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.debug("Extraindo claim específica do token JWT");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Verifica se o token expirou.
     */
    @Override
    public Boolean isTokenExpired(String token) {
        log.debug("Verificando expiração do token JWT");
        Date expiration = extractClaim(token, Claims::getExpiration);
        boolean expired = expiration.before(new Date());
        
        if (expired) {
            log.warn("Token JWT expirou em: {}", expiration);
        } else {
            log.debug("Token JWT válido até: {}", expiration);
        }
        
        return expired;
    }

    /**
     * Extrai todas as claims do token JWT.
     */
    @Override
    public Claims extractAllClaims(String token) {
        log.debug("Extraindo todas as claims do token JWT");
        
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            log.debug("Claims extraídas com sucesso. Subject: {}, Expiration: {}", 
                    claims.getSubject(), claims.getExpiration());
            
            return claims;
            
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
            throw new JwtAuthenticationException("Token expirado", e);
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT não suportado: {}", e.getMessage());
            throw new JwtAuthenticationException("Token não suportado", e);
        } catch (MalformedJwtException e) {
            log.error("Token JWT malformado: {}", e.getMessage());
            throw new JwtAuthenticationException("Token malformado", e);
        } catch (SignatureException e) {
            log.error("Assinatura JWT inválida: {}", e.getMessage());
            throw new JwtAuthenticationException("Assinatura inválida", e);
        } catch (IllegalArgumentException e) {
            log.error("Token JWT vazio ou nulo: {}", e.getMessage());
            throw new JwtAuthenticationException("Token vazio ou nulo", e);
        } catch (JwtException e) {
            log.error("Erro genérico ao processar token JWT: {}", e.getMessage());
            throw new JwtAuthenticationException("Token inválido", e);
        }
    }

    /**
     * Extrai as authorities/roles do token JWT.
     */
    @Override
    public String extractAuthorities(String token) {
        log.debug("Extraindo authorities do token JWT");
        String authorities = extractClaim(token, claims -> claims.get("authorities", String.class));
        log.debug("Authorities extraídas: {}", authorities);
        return authorities;
    }

    /**
     * Cria um token JWT com as claims fornecidas.
     */
    private String createToken(Map<String, Object> claims, String userName) {
        log.debug("Criando token JWT para usuário: {} com {} claims extras", userName, claims.size());
        
        Date now = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);
        
        log.debug("Token será válido de {} até {}", now, expiration);
        
        String token = Jwts.builder()
                .claims(claims)
                .subject(userName)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
        
        log.debug("Token JWT criado com sucesso para usuário: {}", userName);
        
        return token;
    }

    /**
     * Obtém a chave secreta para assinatura dos tokens.
     */
    private SecretKey getSignInKey() {
        log.debug("Obtendo chave de assinatura JWT");
        
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.error("Secret key JWT não configurada!");
            throw new IllegalStateException("JWT secret key não pode ser nula ou vazia");
        }
        
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            
            log.debug("Chave de assinatura JWT obtida com sucesso");
            
            return key;
        } catch (IllegalArgumentException e) {
            log.error("Erro ao decodificar chave JWT Base64: {}", e.getMessage());
            throw new IllegalStateException("Chave JWT deve estar em formato Base64 válido", e);
        }
    }

    /**
     * Gera um token JWT para um usuário específico usando email, ID e tipo.
     */
    @Override
    public String gerarToken(String email, Long usuarioId, TipoUsuario tipoUsuario) {
        log.debug("Gerando token JWT para usuário: {} (ID: {}, Tipo: {})", email, usuarioId, tipoUsuario);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuarioId);
        claims.put("authorities", "ROLE_" + tipoUsuario.name());
        
        return createToken(claims, email);
    }

    /**
     * Valida se um token JWT é válido (sem UserDetails).
     */
    @Override
    public Boolean validarToken(String token) {
        log.debug("Validando token JWT (método simplificado)");
        
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtAuthenticationException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrai o email do usuário do token JWT.
     */
    @Override
    public String extrairEmailDoToken(String token) {
        log.debug("Extraindo email do token JWT");
        return extractUsername(token);
    }

    /**
     * Extrai o ID do usuário do token JWT.
     */
    @Override
    public Long extrairUsuarioIdDoToken(String token) {
        log.debug("Extraindo ID do usuário do token JWT");
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
}