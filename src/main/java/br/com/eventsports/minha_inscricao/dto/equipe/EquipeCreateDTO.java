package br.com.eventsports.minha_inscricao.dto.equipe;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para criação administrativa de uma nova equipe")
public class EquipeCreateDTO {

    @NotBlank(message = "Nome da equipe é obrigatório")
    @Size(min = 3, max = 100, message = "Nome da equipe deve ter entre 3 e 100 caracteres")
    @Schema(description = "Nome da equipe", example = "Warriors CrossFit", required = true, maxLength = 100, minLength = 3)
    private String nome;

    @NotNull(message = "Categoria é obrigatória")
    @Schema(description = "ID da categoria em que a equipe irá competir", example = "1", required = true)
    private Long categoriaId;
}
