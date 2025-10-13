package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados resumidos de um resultado do leaderboard")
public class LeaderboardSummaryDTO {

    @Schema(description = "ID único do resultado", example = "1")
    private Long id;

    @Schema(description = "ID do evento", example = "1")
    private Long eventoId;

    @Schema(description = "ID do workout", example = "1")
    private Long workoutId;

    @Schema(description = "ID da categoria", example = "1")
    private Long categoriaId;

    @Schema(description = "ID do atleta (null se for equipe)", example = "1")
    private Long atletaId;

    @Schema(description = "ID da equipe (null se for atleta)", example = "1")
    private Long equipeId;

    @Schema(description = "Posição da equipe/atleta neste workout específico", example = "1")
    private Integer posicaoWorkout;

    @Schema(description = "Nome do participante (equipe ou atleta)", example = "Equipe Alpha")
    private String nomeParticipante;

    @Schema(description = "Nome do workout", example = "21-15-9 Thrusters/Pull-ups")
    private String nomeWorkout;

    @Schema(description = "Nome da categoria", example = "Masculino Elite")
    private String nomeCategoria;

    @Schema(description = "Resultado bruto (valor sem formatação)", example = "150")
    private String resultadoValor;

    @Schema(description = "Resultado formatado com unidade", example = "150 reps")
    private String resultadoFormatado;

    @Schema(description = "Pontuação obtida neste workout", example = "100")
    private Integer pontuacaoWorkout;

    @Schema(description = "Indica se é uma equipe (true) ou atleta individual (false)", example = "false")
    private Boolean isEquipe;

    @Schema(description = "Indica se o participante finalizou este workout", example = "true")
    private Boolean finalizado;

    @Schema(description = "Indica se está no pódio deste workout", example = "true")
    private Boolean isPodioWorkout;

    @Schema(description = "Medalha do pódio deste workout", example = "🥇")
    private String medalhaWorkout;

    @Schema(description = "Indica se é categoria de equipe", example = "true")
    private Boolean isCategoriaEquipe;
}
