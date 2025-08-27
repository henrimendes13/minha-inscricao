package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados mínimos de um evento para leaderboard")
public class EventoMinimalDTO {
    
    @Schema(description = "ID do evento", example = "1")
    private Long id;
    
    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nome;
}