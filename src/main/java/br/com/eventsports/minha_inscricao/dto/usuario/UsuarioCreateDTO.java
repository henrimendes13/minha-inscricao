package br.com.eventsports.minha_inscricao.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação de usuário - Tipo determinado dinamicamente pelas ações")
public class UsuarioCreateDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    @Schema(description = "Email do usuário (login)", example = "joao@exemplo.com", required = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 5, max = 100, message = "Senha deve ter entre 5 e 100 caracteres")
    @Schema(description = "Senha do usuário", example = "minhasenha123", required = true)
    private String senha;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Schema(description = "Nome completo do usuário", example = "João Silva Santos", required = true)
    private String nome;

    @Builder.Default
    @Schema(description = "Aceita os termos de uso", example = "true")
    private Boolean aceitaTermos = true;
}
