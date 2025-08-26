package br.com.eventsports.minha_inscricao.dto.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO simplificado para atualizar resultado de workout")
public class WorkoutResultUpdateDTO {

    @Schema(description = "Valor do resultado (pode ser Integer, Double ou String dependendo do tipo do workout)", 
            example = "150")
    private Object resultadoValor;

    @Schema(description = "Indica se o participante finalizou o workout", example = "true")
    private Boolean finalizado;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Schema(description = "Observações sobre a performance")
    private String observacoes;
}