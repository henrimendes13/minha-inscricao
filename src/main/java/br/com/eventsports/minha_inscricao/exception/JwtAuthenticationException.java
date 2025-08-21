package br.com.eventsports.minha_inscricao.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exceção customizada para erros relacionados à autenticação JWT.
 * 
 * Esta exceção é lançada quando:
 * - Token JWT é inválido ou malformado
 * - Token JWT expirou
 * - Assinatura do token é inválida
 * - Token não possui claims necessárias
 */
public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(String message) {
        super(message);
    }

    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}