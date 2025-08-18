package br.com.eventsports.minha_inscricao.dto.workout;

import br.com.eventsports.minha_inscricao.enums.TipoWorkout;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualização de um workout")
public class WorkoutUpdateDTO {

    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Schema(description = "Nome do workout", example = "21-15-9 Thrusters/Pull-ups", maxLength = 200)
    private String nome;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Schema(description = "Descrição detalhada do workout", 
            example = "For time: 21-15-9 Thrusters 95/65 Pull-ups", 
            maxLength = 5000)
    private String descricao;

    @Schema(description = "Tipo de resultado do workout", example = "REPS",
            allowableValues = {"REPS", "PESO", "TEMPO"})
    private TipoWorkout tipo;

    @Schema(description = "Lista de IDs das categorias associadas ao workout", 
            example = "[1, 2, 3]")
    private List<Long> categoriasIds;

    // Campos de resultado baseados no tipo
    @PositiveOrZero(message = "Número de repetições deve ser positivo ou zero")
    @Schema(description = "Resultado em repetições (apenas para tipo REPS)", example = "150")
    private Integer resultadoReps;

    @DecimalMin(value = "0.0", inclusive = true, message = "Peso deve ser maior ou igual a zero")
    @Digits(integer = 6, fraction = 2, message = "Peso deve ter no máximo 6 dígitos inteiros e 2 decimais")
    @Schema(description = "Resultado em peso (apenas para tipo PESO)", example = "120.50")
    private Double resultadoPeso;

    @Schema(description = "Resultado em tempo no formato mm:ss ou hh:mm:ss (apenas para tipo TEMPO)", 
            example = "2:30")
    private String resultadoTempo;

    @Schema(description = "Indica se o workout está ativo", example = "true")
    private Boolean ativo;
}
