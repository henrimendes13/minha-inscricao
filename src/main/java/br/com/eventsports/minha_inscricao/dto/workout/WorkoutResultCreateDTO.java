package br.com.eventsports.minha_inscricao.dto.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO simplificado para registrar resultado de workout")
public class WorkoutResultCreateDTO {

    @NotNull(message = "ID da categoria é obrigatório")
    @Positive(message = "ID da categoria deve ser um número positivo")
    @Schema(description = "ID da categoria", example = "1", required = true)
    private Long categoriaId;

    @NotNull(message = "ID do participante é obrigatório")
    @Positive(message = "ID do participante deve ser um número positivo")
    @Schema(description = "ID do participante (equipe ou atleta)", example = "1", required = true)
    private Long participanteId;

    @NotNull(message = "Tipo de participante é obrigatório")
    @Schema(description = "Indica se o participante é uma equipe ou atleta individual", 
            example = "true", required = true)
    private Boolean isEquipe;

    @Schema(description = "Valor do resultado (pode ser Integer, Double ou String dependendo do tipo do workout)", 
            example = "150")
    private Object resultadoValor;

    @Schema(description = "Indica se o participante finalizou o workout", example = "true")
    @Builder.Default
    private Boolean finalizado = false;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Schema(description = "Observações sobre a performance")
    private String observacoes;
}