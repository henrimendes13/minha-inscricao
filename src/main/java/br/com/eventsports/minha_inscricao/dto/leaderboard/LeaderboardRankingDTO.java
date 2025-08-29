package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para item do ranking do leaderboard")
public class LeaderboardRankingDTO {

    @Schema(description = "Posição no ranking", example = "1")
    private Integer posicao;

    @Schema(description = "Nome do participante (atleta ou equipe)", example = "João Silva")
    private String nomeParticipante;

    @Schema(description = "Pontuação total (soma das posições nos workouts)", example = "25")
    private Integer pontuacaoTotal;

    @Schema(description = "Indica se é uma equipe (true) ou atleta individual (false)", example = "false")
    private Boolean isEquipe;

    @Schema(description = "ID do participante (atleta ou equipe)", example = "107")
    private Long participanteId;

    @Schema(description = "Nome da categoria", example = "RX Masculino")
    private String nomeCategoria;

    @Schema(description = "Número de workouts completados", example = "5")
    private Long workoutsCompletados;

    @Schema(description = "Se está no pódio (top 3)", example = "true")
    private Boolean isPodio;

    @Schema(description = "Tipo de medalha se estiver no pódio", example = "OURO")
    private String medalha;

    public Boolean isPodio() {
        return this.posicao != null && this.posicao <= 3;
    }

    public String getMedalha() {
        if (!isPodio()) return "";
        
        return switch (this.posicao) {
            case 1 -> "OURO";
            case 2 -> "PRATA";
            case 3 -> "BRONZE";
            default -> "";
        };
    }
}