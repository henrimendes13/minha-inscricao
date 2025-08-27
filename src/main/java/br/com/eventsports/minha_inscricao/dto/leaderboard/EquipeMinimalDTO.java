package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados mínimos de uma equipe para leaderboard")
public class EquipeMinimalDTO {
    
    @Schema(description = "ID da equipe", example = "1")
    private Long id;
    
    @Schema(description = "Nome da equipe", example = "Crias CrossFit")
    private String nome;
}