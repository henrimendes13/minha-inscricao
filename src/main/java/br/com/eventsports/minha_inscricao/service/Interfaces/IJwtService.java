package br.com.eventsports.minha_inscricao.service.Interfaces;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

/**
 * Interface para o serviço JWT responsável por gerenciar tokens de autenticação.
 * 
 * Este serviço fornece funcionalidades para:
 * - Gerar tokens JWT para usuários autenticados
 * - Validar tokens JWT
 * - Extrair informações dos tokens (username, claims, etc.)
 * - Verificar expiração de tokens
 */
public interface IJwtService {

    /**
     * Gera um token JWT para o usuário fornecido.
     * O token incluirá automaticamente as roles/authorities do usuário.
     *
     * @param userDetails Detalhes do usuário para o qual gerar o token
     * @return Token JWT assinado
     */
    String generateToken(UserDetails userDetails);

    /**
     * Gera um token JWT com claims customizadas.
     *
     * @param extraClaims Claims adicionais para incluir no token
     * @param userDetails Detalhes do usuário para o qual gerar o token
     * @return Token JWT assinado
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Valida se um token é válido para o usuário fornecido.
     * Verifica tanto a integridade do token quanto se não expirou.
     *
     * @param token Token JWT a ser validado
     * @param userDetails Detalhes do usuário para validação
     * @return true se o token é válido, false caso contrário
     */
    Boolean validateToken(String token, UserDetails userDetails);

    /**
     * Extrai o username (subject) do token JWT.
     *
     * @param token Token JWT
     * @return Username extraído do token
     */
    String extractUsername(String token);

    /**
     * Extrai uma claim específica do token usando um resolver customizado.
     *
     * @param token Token JWT
     * @param claimsResolver Função para extrair a claim desejada
     * @param <T> Tipo da claim a ser extraída
     * @return Valor da claim extraída
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    /**
     * Verifica se o token expirou.
     *
     * @param token Token JWT a ser verificado
     * @return true se o token expirou, false caso contrário
     */
    Boolean isTokenExpired(String token);

    /**
     * Extrai todas as claims do token JWT.
     *
     * @param token Token JWT
     * @return Todas as claims do token
     */
    Claims extractAllClaims(String token);

    /**
     * Extrai as authorities/roles do token JWT.
     *
     * @param token Token JWT
     * @return Lista de authorities como String
     */
    String extractAuthorities(String token);

    /**
     * Gera um token JWT para um usuário específico usando email, ID e tipo.
     * Método de conveniência para casos onde não temos um UserDetails completo.
     *
     * @param email Email do usuário
     * @param usuarioId ID do usuário
     * @param tipoUsuario Tipo do usuário
     * @return Token JWT assinado
     */
    String gerarToken(String email, Long usuarioId, br.com.eventsports.minha_inscricao.enums.TipoUsuario tipoUsuario);

    /**
     * Valida se um token JWT é válido (não expirado e íntegro).
     * Método simplificado que não requer UserDetails.
     *
     * @param token Token JWT a ser validado
     * @return true se o token é válido, false caso contrário
     */
    Boolean validarToken(String token);

    /**
     * Extrai o email do usuário do token JWT.
     * Método de conveniência que usa extractUsername.
     *
     * @param token Token JWT
     * @return Email do usuário extraído do token
     */
    String extrairEmailDoToken(String token);

    /**
     * Extrai o ID do usuário do token JWT.
     *
     * @param token Token JWT
     * @return ID do usuário extraído do token
     */
    Long extrairUsuarioIdDoToken(String token);
}