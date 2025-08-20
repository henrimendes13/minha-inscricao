package br.com.eventsports.minha_inscricao.security;

import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utilitários para facilitar o acesso aos dados do usuário autenticado.
 */
public class SecurityUtils {

    /**
     * Obtém o CustomUserPrincipal do usuário autenticado atual.
     */
    public static Optional<CustomUserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof CustomUserPrincipal) {
            return Optional.of((CustomUserPrincipal) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }

    /**
     * Obtém o ID do usuário autenticado atual.
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(CustomUserPrincipal::getId);
    }

    /**
     * Obtém o email do usuário autenticado atual.
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(CustomUserPrincipal::getEmail);
    }

    /**
     * Obtém o nome do usuário autenticado atual.
     */
    public static Optional<String> getCurrentUserName() {
        return getCurrentUser().map(CustomUserPrincipal::getNome);
    }

    /**
     * Obtém o tipo do usuário autenticado atual.
     */
    public static Optional<TipoUsuario> getCurrentUserType() {
        return getCurrentUser().map(CustomUserPrincipal::getTipo);
    }

    /**
     * Verifica se o usuário atual é um organizador.
     */
    public static boolean isCurrentUserOrganizador() {
        return getCurrentUser().map(CustomUserPrincipal::isOrganizador).orElse(false);
    }

    /**
     * Verifica se o usuário atual é um atleta.
     */
    public static boolean isCurrentUserAtleta() {
        return getCurrentUser().map(CustomUserPrincipal::isAtleta).orElse(false);
    }

    /**
     * Verifica se há um usuário autenticado.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               authentication.getPrincipal() instanceof CustomUserPrincipal;
    }
}