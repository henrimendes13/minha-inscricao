package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para posição de um workout específico no ranking")
public class WorkoutPosicaoDTO {

    @Schema(description = "ID do workout", example = "1")
    private Long workoutId;

    @Schema(description = "Nome do workout", example = "WOD 1 - AMRAP")
    private String nomeWorkout;

    @Schema(description = "Posição do participante no workout", example = "3")
    private Integer posicaoWorkout;

    @Schema(description = "Resultado formatado do participante", example = "150 reps")
    private String resultadoFormatado;
}