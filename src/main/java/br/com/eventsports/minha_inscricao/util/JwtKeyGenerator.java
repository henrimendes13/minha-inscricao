package br.com.eventsports.minha_inscricao.util;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Utilitário para gerar chaves secretas JWT válidas em formato Base64.
 * 
 * Esta classe é utilizada apenas para geração de chaves durante desenvolvimento.
 * A chave gerada deve ser copiada para o arquivo application.properties.
 */
@Slf4j
public class JwtKeyGenerator {

    /**
     * Gera uma chave secreta segura para JWT em formato Base64.
     * Utiliza o algoritmo HMAC-SHA256 que requer pelo menos 256 bits (32 bytes).
     * 
     * @return Chave secreta em formato Base64
     */
    public static String generateSecretKey() {
        log.info("Gerando nova chave secreta JWT...");
        
        // Gera uma chave segura para HMAC-SHA256
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        
        // Converte para Base64
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        
        log.info("Chave secreta JWT gerada com sucesso. Tamanho: {} bytes", key.getEncoded().length);
        log.debug("Chave gerada: {}", base64Key);
        
        return base64Key;
    }

    /**
     * Converte uma string existente para Base64 (caso necessário).
     * 
     * @param plainText String para converter
     * @return String em formato Base64
     */
    public static String encodeToBase64(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new IllegalArgumentException("Texto não pode ser nulo ou vazio");
        }
        
        log.debug("Convertendo string para Base64...");
        String encoded = Base64.getEncoder().encodeToString(plainText.getBytes());
        log.debug("Conversão concluída. Tamanho original: {} bytes, Base64: {} chars", 
                plainText.getBytes().length, encoded.length());
        
        return encoded;
    }

    /**
     * Verifica se uma string está em formato Base64 válido.
     * 
     * @param input String para verificar
     * @return true se estiver em Base64 válido
     */
    public static boolean isValidBase64(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        try {
            Base64.getDecoder().decode(input);
            return true;
        } catch (IllegalArgumentException e) {
            log.debug("String não está em formato Base64 válido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Método principal para testes e geração de chaves.
     */
    public static void main(String[] args) {
        System.out.println("=== Gerador de Chaves JWT ===");
        
        // Gera nova chave
        String newKey = generateSecretKey();
        System.out.println("Nova chave JWT (Base64): " + newKey);
        
        // Converte chave existente
        String existingKey = "minhaSuperChaveSecretaParaJWTQueDeveSerMuitoLongaESegura123!@#";
        String convertedKey = encodeToBase64(existingKey);
        System.out.println("Chave existente convertida (Base64): " + convertedKey);
        
        // Verifica validade
        System.out.println("Nova chave é Base64 válido: " + isValidBase64(newKey));
        System.out.println("Chave convertida é Base64 válido: " + isValidBase64(convertedKey));
    }
}