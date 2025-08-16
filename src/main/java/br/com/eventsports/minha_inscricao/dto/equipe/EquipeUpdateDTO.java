package br.com.eventsports.minha_inscricao.dto.equipe;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualização de uma equipe")
public class EquipeUpdateDTO {

    @NotBlank(message = "Nome da equipe é obrigatório")
    @Size(min = 3, max = 100, message = "Nome da equipe deve ter entre 3 e 100 caracteres")
    @Schema(description = "Nome da equipe", example = "Warriors CrossFit Elite", required = true, maxLength = 100, minLength = 3)
    private String nome;

    @NotNull(message = "Categoria é obrigatória")
    @Schema(description = "ID da categoria em que a equipe irá competir", example = "1", required = true)
    private Long categoriaId;

    @Schema(description = "Indica se a equipe está ativa", example = "true")
    private Boolean ativa;

    @Size(min = 1, max = 6, message = "Uma equipe deve ter entre 1 e 6 atletas")
    @Schema(description = "Lista de IDs dos atletas que farão parte da equipe (1-6 atletas) - opcional, se não informado mantém os atletas atuais", 
            example = "[1, 2, 3]")
    private List<Long> atletasIds;

    @Schema(description = "ID do atleta que será capitão - opcional, se não informado mantém o capitão atual", 
            example = "1")
    private Long capitaoId;
}
