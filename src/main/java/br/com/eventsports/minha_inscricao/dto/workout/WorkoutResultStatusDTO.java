package br.com.eventsports.minha_inscricao.dto.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO com status e estatísticas de resultados de um workout")
public class WorkoutResultStatusDTO {

    @Schema(description = "ID do workout", example = "1")
    private Long workoutId;

    @Schema(description = "Nome do workout", example = "Deadlift 1RM")
    private String nomeWorkout;

    @Schema(description = "ID da categoria", example = "1")
    private Long categoriaId;

    @Schema(description = "Nome da categoria", example = "RX Masculino")
    private String nomeCategoria;

    @Schema(description = "Total de participantes no workout", example = "25")
    private Long totalParticipantes;

    @Schema(description = "Número de participantes que finalizaram", example = "18")
    private Long participantesFinalizados;

    @Schema(description = "Porcentagem de participantes que finalizaram", example = "72.0")
    private Double porcentagemFinalizados;

    @Schema(description = "Indica se o workout foi finalizado (todos finalizaram)", example = "false")
    private Boolean workoutFinalizado;

    @Schema(description = "Lista de participantes que ainda não finalizaram")
    private List<String> participantesPendentes;
}