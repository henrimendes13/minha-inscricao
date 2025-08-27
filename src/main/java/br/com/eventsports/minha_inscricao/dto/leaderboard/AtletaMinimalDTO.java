package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados mínimos de um atleta para leaderboard")
public class AtletaMinimalDTO {
    
    @Schema(description = "ID do atleta", example = "1")
    private Long id;
    
    @Schema(description = "Nome do atleta", example = "João Silva")
    private String nome;
}