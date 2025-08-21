package br.com.eventsports.minha_inscricao.dto.auth;

import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resposta do login")
public class LoginResponseDTO {

    @Schema(description = "Mensagem de sucesso do login", example = "Login realizado com sucesso")
    private String mensagem;

    @Schema(description = "Token JWT para autenticação", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Dados completos do usuário logado")
    private UsuarioResponseDTO usuario;
}