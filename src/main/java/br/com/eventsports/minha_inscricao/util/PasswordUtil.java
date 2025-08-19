package br.com.eventsports.minha_inscricao.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordUtil {

    private final PasswordEncoder passwordEncoder;

    /**
     * Codifica uma senha usando BCrypt
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifica se uma senha raw corresponde à senha codificada
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
