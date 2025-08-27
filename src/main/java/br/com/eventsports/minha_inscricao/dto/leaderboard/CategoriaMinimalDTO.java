package br.com.eventsports.minha_inscricao.dto.leaderboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados mínimos de uma categoria para leaderboard")
public class CategoriaMinimalDTO {
    
    @Schema(description = "ID da categoria", example = "1")
    private Long id;
    
    @Schema(description = "Nome da categoria", example = "Trio Masculino RX")
    private String nome;
}