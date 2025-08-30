package br.com.eventsports.minha_inscricao.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponseDTO {

    private String token;
    
    private String tokenType;
    
    private long expiresIn;
    
    private String email;
    
    private String nome;
    
    private Long userId;
    
    private String tipoUsuario;
    
    private LocalDateTime loginAt;

    public static AdminLoginResponseDTO success(String token, String email, String nome, Long userId, long expiresIn) {
        return AdminLoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .email(email)
                .nome(nome)
                .userId(userId)
                .tipoUsuario("ADMIN")
                .loginAt(LocalDateTime.now())
                .build();
    }
}