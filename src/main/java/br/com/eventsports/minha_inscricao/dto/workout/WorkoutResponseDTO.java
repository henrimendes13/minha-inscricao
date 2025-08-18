package br.com.eventsports.minha_inscricao.dto.workout;

import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoSummaryDTO;
import br.com.eventsports.minha_inscricao.enums.TipoWorkout;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de resposta de um workout")
public class WorkoutResponseDTO {

    @Schema(description = "ID único do workout", example = "1")
    private Long id;

    @Schema(description = "Nome do workout", example = "21-15-9 Thrusters/Pull-ups")
    private String nome;

    @Schema(description = "Descrição detalhada do workout", 
            example = "For time: 21-15-9 Thrusters 95/65 Pull-ups")
    private String descricao;

    @Schema(description = "Tipo de resultado do workout", example = "REPS")
    private TipoWorkout tipo;

    @Schema(description = "Indica se o workout está ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Evento ao qual o workout pertence")
    private EventoSummaryDTO evento;

    @Schema(description = "Lista de categorias associadas ao workout")
    private List<CategoriaSummaryDTO> categorias;

    @Schema(description = "Quantidade de categorias associadas", example = "3")
    private Integer quantidadeCategorias;

    @Schema(description = "Nomes das categorias separados por vírgula", 
            example = "Masculino Elite, Feminino Elite, Masters")
    private String nomesCategorias;

    // Campos de resultado baseados no tipo
    @Schema(description = "Resultado em repetições (apenas para tipo REPS)", example = "150")
    private Integer resultadoReps;

    @Schema(description = "Resultado em peso (apenas para tipo PESO)", example = "120.50")
    private Double resultadoPeso;

    @Schema(description = "Resultado em tempo como string formatada (apenas para tipo TEMPO)", example = "2:30")
    private String resultadoTempo;

    @Schema(description = "Resultado em segundos (apenas para tipo TEMPO)", example = "150")
    private Integer resultadoTempoSegundos;

    @Schema(description = "Resultado formatado com unidade", example = "150 reps")
    private String resultadoFormatado;

    @Schema(description = "Unidade de medida baseada no tipo", example = "repetições")
    private String unidadeMedida;

    @Schema(description = "Indica se tem resultado definido", example = "true")
    private Boolean temResultado;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização")
    private LocalDateTime updatedAt;
}
