package br.com.eventsports.minha_inscricao.dto.leaderboard;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Classificação final de uma categoria (ranking geral)")
public class LeaderboardFinalDTO {

    @Schema(description = "Posição final na categoria", example = "1")
    private Integer posicaoFinal;

    @Schema(description = "Pontuação total (soma das posições nos workouts)", example = "8")
    private Integer pontuacaoTotal;

    @Schema(description = "Evento da classificação")
    private EventoSummaryDTO evento;

    @Schema(description = "Categoria da classificação")
    private CategoriaSummaryDTO categoria;

    @Schema(description = "Equipe (apenas se categoria for do tipo EQUIPE)")
    private EquipeSummaryDTO equipe;

    @Schema(description = "Atleta (apenas se categoria for do tipo INDIVIDUAL)")
    private AtletaSummaryDTO atleta;

    @Schema(description = "Nome do participante (equipe ou atleta)", example = "Equipe Alpha")
    private String nomeParticipante;

    @Schema(description = "Lista de resultados individuais por workout")
    private List<LeaderboardSummaryDTO> resultadosWorkouts;

    @Schema(description = "Quantidade de workouts finalizados", example = "3")
    private Integer workoutsFinalizados;

    @Schema(description = "Quantidade total de workouts da categoria", example = "3")
    private Integer totalWorkouts;

    @Schema(description = "Indica se finalizou todos os workouts", example = "true")
    private Boolean finalizouTodos;

    @Schema(description = "Indica se é categoria de equipe", example = "true")
    private Boolean isCategoriaEquipe;

    @Schema(description = "Indica se está no pódio final", example = "true")
    private Boolean isPodioFinal;

    @Schema(description = "Medalha do pódio final", example = "🥇")
    private String medalhaFinal;

    @Schema(description = "Descrição da performance", 
            example = "1º lugar: Equipe Alpha - 8 pts (3/3 workouts finalizados)")
    private String descricaoPerformance;
}
