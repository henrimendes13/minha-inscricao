package br.com.eventsports.minha_inscricao.dto.leaderboard;

import br.com.eventsports.minha_inscricao.dto.leaderboard.AtletaMinimalDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.CategoriaMinimalDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.EquipeMinimalDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.EventoMinimalDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.WorkoutMinimalDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de resposta de um resultado do leaderboard")
public class LeaderboardResponseDTO {

    @Schema(description = "ID único do resultado", example = "1")
    private Long id;

    @Schema(description = "Posição da equipe/atleta neste workout específico", example = "1")
    private Integer posicaoWorkout;

    @Schema(description = "Indica se o participante finalizou este workout", example = "true")
    private Boolean finalizado;

    @Schema(description = "Evento da classificação")
    private EventoMinimalDTO evento;

    @Schema(description = "Categoria da classificação")
    private CategoriaMinimalDTO categoria;

    @Schema(description = "Workout específico")
    private WorkoutMinimalDTO workout;

    @Schema(description = "Equipe (apenas se categoria for do tipo EQUIPE)")
    private EquipeMinimalDTO equipe;

    @Schema(description = "Atleta (apenas se categoria for do tipo INDIVIDUAL)")
    private AtletaMinimalDTO atleta;

    // Campos de resultado baseados no tipo do workout
    @Schema(description = "Resultado em repetições (apenas para workout tipo REPS)", example = "150")
    private Integer resultadoReps;

    @Schema(description = "Resultado em peso (apenas para workout tipo PESO)", example = "120.50")
    private Double resultadoPeso;

    @Schema(description = "Resultado em tempo como string formatada (apenas para workout tipo TEMPO)", example = "2:30")
    private String resultadoTempo;

    @Schema(description = "Resultado em segundos (apenas para workout tipo TEMPO)", example = "150")
    private Integer resultadoTempoSegundos;

    @Schema(description = "Resultado formatado com unidade", example = "150 reps")
    private String resultadoFormatado;

    @Schema(description = "Nome do participante (equipe ou atleta)", example = "Equipe Alpha")
    private String nomeParticipante;

    @Schema(description = "Indica se é categoria de equipe", example = "true")
    private Boolean isCategoriaEquipe;

    @Schema(description = "Indica se tem resultado definido", example = "true")
    private Boolean temResultado;

    @Schema(description = "Indica se está no pódio deste workout", example = "true")
    private Boolean isPodioWorkout;

    @Schema(description = "Medalha do pódio deste workout", example = "🥇")
    private String medalhaWorkout;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização")
    private LocalDateTime updatedAt;
}
