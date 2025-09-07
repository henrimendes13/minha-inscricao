package br.com.eventsports.minha_inscricao.dto.workout;

import br.com.eventsports.minha_inscricao.enums.TipoWorkout;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados resumidos de um workout")
public class WorkoutSummaryDTO {

    @Schema(description = "ID único do workout", example = "1")
    private Long id;

    @Schema(description = "Nome do workout", example = "21-15-9 Thrusters/Pull-ups")
    private String nome;

    @Schema(description = "Descrição do workout", example = "For time: 21-15-9 Thrusters 95/65 Pull-ups")
    private String descricao;

    @Schema(description = "Tipo de resultado do workout", example = "REPS")
    private TipoWorkout tipo;

    @Schema(description = "Indica se o workout está ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Quantidade de categorias associadas", example = "3")
    private Integer quantidadeCategorias;

    @Schema(description = "Nomes das categorias separados por vírgula", 
            example = "Masculino Elite, Feminino Elite")
    private String nomesCategorias;

    @Schema(description = "Nome do evento", example = "CrossFit Open 2024")
    private String nomeEvento;

    @Schema(description = "Unidade de medida baseada no tipo", example = "repetições")
    private String unidadeMedida;
}
