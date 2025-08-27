package br.com.eventsports.minha_inscricao.dto.leaderboard;

import br.com.eventsports.minha_inscricao.enums.TipoWorkout;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados mínimos de um workout para leaderboard")
public class WorkoutMinimalDTO {
    
    @Schema(description = "ID do workout", example = "1")
    private Long id;
    
    @Schema(description = "Nome do workout", example = "Fran")
    private String nome;
    
    @Schema(description = "Tipo do workout", example = "REPS")
    private TipoWorkout tipo;
}