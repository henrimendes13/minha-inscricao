package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualizar resultado de um participante em um workout")
public class LeaderboardResultadoUpdateDTO {

    // Campos de resultado baseados no tipo do workout
    @PositiveOrZero(message = "Número de repetições deve ser positivo ou zero")
    @Schema(description = "Resultado em repetições (apenas para workout tipo REPS)", example = "150")
    private Integer resultadoReps;

    @DecimalMin(value = "0.0", inclusive = true, message = "Peso deve ser maior ou igual a zero")
    @Digits(integer = 6, fraction = 2, message = "Peso deve ter no máximo 6 dígitos inteiros e 2 decimais")
    @Schema(description = "Resultado em peso (apenas para workout tipo PESO)", example = "120.50")
    private Double resultadoPeso;

    @Schema(description = "Resultado em tempo no formato mm:ss ou hh:mm:ss (apenas para workout tipo TEMPO)", 
            example = "2:30")
    private String resultadoTempo;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Schema(description = "Observações sobre a performance", 
            example = "Resultado corrigido")
    private String observacoes;

    @Schema(description = "Indica se o participante finalizou o workout", example = "true")
    private Boolean finalizado;
}
