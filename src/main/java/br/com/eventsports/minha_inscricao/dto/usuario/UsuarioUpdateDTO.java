package br.com.eventsports.minha_inscricao.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para atualização de usuário")
public class UsuarioUpdateDTO {

    @Email(message = "Email deve ter um formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    @Schema(description = "Email do usuário (login)", example = "joao@exemplo.com")
    private String email;

    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    @Schema(description = "Nova senha do usuário", example = "novasenha123")
    private String senha;

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Schema(description = "Nome completo do usuário", example = "João Silva Santos")
    private String nome;

    @Schema(description = "Indica se o usuário está ativo", example = "true")
    private Boolean ativo;
}
