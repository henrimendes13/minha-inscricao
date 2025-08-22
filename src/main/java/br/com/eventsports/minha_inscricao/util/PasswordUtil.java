package br.com.eventsports.minha_inscricao.util;

import org.springframework.stereotype.Component;

@Component
public class PasswordUtil {

    /**
     * Retorna a senha sem codificação (security removed)
     */
    public String encode(String rawPassword) {
        return rawPassword;
    }

    /**
     * Verifica se duas senhas são iguais
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }
}
