package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para registrar múltiplos resultados em lote")
public class LeaderboardResultadoLoteDTO {

    @NotNull(message = "ID da categoria é obrigatório")
    @Positive(message = "ID da categoria deve ser um número positivo")
    @Schema(description = "ID da categoria", example = "1", required = true)
    private Long categoriaId;

    @NotNull(message = "ID do workout é obrigatório")
    @Positive(message = "ID do workout deve ser um número positivo")
    @Schema(description = "ID do workout", example = "1", required = true)
    private Long workoutId;

    @NotNull(message = "Lista de resultados é obrigatória")
    @NotEmpty(message = "Lista de resultados não pode estar vazia")
    @Valid
    @Schema(description = "Lista de resultados para registrar")
    private List<ResultadoLoteItemDTO> resultados;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Item individual de resultado no lote")
    public static class ResultadoLoteItemDTO {

        @Positive(message = "ID da equipe deve ser um número positivo")
        @Schema(description = "ID da equipe (apenas se categoria for EQUIPE)", example = "1")
        private Long equipeId;

        @Positive(message = "ID do atleta deve ser um número positivo")
        @Schema(description = "ID do atleta (apenas se categoria for INDIVIDUAL)", example = "1")
        private Long atletaId;

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
                example = "Excelente performance")
        private String observacoes;

        @Schema(description = "Indica se o participante finalizou o workout", example = "true")
        private Boolean finalizado;
    }
}
