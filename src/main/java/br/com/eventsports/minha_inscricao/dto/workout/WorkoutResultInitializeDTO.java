package br.com.eventsports.minha_inscricao.dto.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para inicializar resultados de um workout para todos os participantes de uma categoria")
public class WorkoutResultInitializeDTO {

    @NotNull(message = "ID do workout é obrigatório")
    @Positive(message = "ID do workout deve ser um número positivo")
    @Schema(description = "ID do workout", example = "1", required = true)
    private Long workoutId;

    @NotNull(message = "ID da categoria é obrigatório")
    @Positive(message = "ID da categoria deve ser um número positivo")
    @Schema(description = "ID da categoria", example = "1", required = true)
    private Long categoriaId;
}