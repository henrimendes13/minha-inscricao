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
@Schema(description = "Dados para criação de um novo workout")
public class WorkoutCreateDTO {

    @NotBlank(message = "Nome do workout é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Schema(description = "Nome do workout", example = "21-15-9 Thrusters/Pull-ups", required = true, maxLength = 200)
    private String nome;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Schema(description = "Descrição detalhada do workout", 
            example = "For time: 21-15-9 Thrusters 95/65 Pull-ups", 
            maxLength = 5000)
    private String descricao;

    @NotNull(message = "Tipo do workout é obrigatório")
    @Schema(description = "Tipo de resultado do workout", example = "REPS", required = true,
            allowableValues = {"REPS", "PESO", "TEMPO"})
    private TipoWorkout tipo;

    @NotNull(message = "Evento é obrigatório")
    @Positive(message = "ID do evento deve ser um número positivo")
    @Schema(description = "ID do evento ao qual o workout pertence", example = "1", required = true)
    private Long eventoId;

    @Schema(description = "Lista de IDs das categorias associadas ao workout", 
            example = "[1, 2, 3]")
    private List<Long> categoriasIds;


    @Schema(description = "Indica se o workout está ativo", example = "true")
    private Boolean ativo;
}
