package br.com.eventsports.minha_inscricao.exception;

/**
 * Exceção lançada quando um usuário tenta acessar um recurso sem as devidas permissões.
 * Corresponde ao HTTP Status 403 Forbidden.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}