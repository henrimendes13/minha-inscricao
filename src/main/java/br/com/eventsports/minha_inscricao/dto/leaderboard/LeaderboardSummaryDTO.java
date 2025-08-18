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

    @Schema(description = "Posição da equipe/atleta neste workout específico", example = "1")
    private Integer posicaoWorkout;

    @Schema(description = "Nome do participante (equipe ou atleta)", example = "Equipe Alpha")
    private String nomeParticipante;

    @Schema(description = "Nome do workout", example = "21-15-9 Thrusters/Pull-ups")
    private String nomeWorkout;

    @Schema(description = "Nome da categoria", example = "Masculino Elite")
    private String nomeCategoria;

    @Schema(description = "Resultado formatado com unidade", example = "150 reps")
    private String resultadoFormatado;

    @Schema(description = "Indica se o participante finalizou este workout", example = "true")
    private Boolean finalizado;

    @Schema(description = "Indica se está no pódio deste workout", example = "true")
    private Boolean isPodioWorkout;

    @Schema(description = "Medalha do pódio deste workout", example = "🥇")
    private String medalhaWorkout;

    @Schema(description = "Indica se é categoria de equipe", example = "true")
    private Boolean isCategoriaEquipe;
}
