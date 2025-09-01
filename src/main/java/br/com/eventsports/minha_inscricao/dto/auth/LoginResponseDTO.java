package br.com.eventsports.minha_inscricao.dto.auth;

import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    
    private String tokenType;
    
    private long expiresIn;
    
    private String email;
    
    private String nome;
    
    private Long userId;
    
    private String tipoUsuario;
    
    private String role;
    
    private LocalDateTime loginAt;

    public static LoginResponseDTO success(String token, String email, String nome, Long userId, TipoUsuario tipoUsuario, long expiresIn) {
        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .email(email)
                .nome(nome)
                .userId(userId)
                .tipoUsuario(tipoUsuario.name())
                .role("ROLE_" + tipoUsuario.name())
                .loginAt(LocalDateTime.now())
                .build();
    }
}