package br.com.eventsports.minha_inscricao.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordUtil {

    /**
     * Codifica uma senha usando SHA-256 com salt
     */
    public String encode(String rawPassword) {
        try {
            // Gerar salt aleatório
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            
            // Criar hash da senha com salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(rawPassword.getBytes());
            
            // Combinar salt + hash e codificar em Base64
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao codificar senha", e);
        }
    }

    /**
     * Verifica se uma senha raw corresponde à senha codificada
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            // Decodificar a senha armazenada
            byte[] combined = Base64.getDecoder().decode(encodedPassword);
            
            // Extrair salt (primeiros 16 bytes)
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, 16);
            
            // Extrair hash original
            byte[] originalHash = new byte[combined.length - 16];
            System.arraycopy(combined, 16, originalHash, 0, originalHash.length);
            
            // Calcular hash da senha fornecida com o mesmo salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] testHash = md.digest(rawPassword.getBytes());
            
            // Comparar os hashes
            return MessageDigest.isEqual(originalHash, testHash);
        } catch (Exception e) {
            return false;
        }
    }
}
