package br.com.eventsports.minha_inscricao.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para requisição de login")
public class LoginRequestDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    @Schema(description = "Email do usuário para login", example = "joao@exemplo.com", required = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    @Schema(description = "Senha do usuário", example = "minhasenha123", required = true)
    private String senha;
}