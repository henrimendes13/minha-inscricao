package br.com.eventsports.minha_inscricao.dto.inscricao;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO com informações de participante (atleta ou equipe) para gerenciamento de resultados")
public class ParticipanteDTO {

    @Schema(description = "ID da inscrição", example = "1", required = true)
    private Long inscricaoId;

    @Schema(description = "ID do atleta ou equipe", example = "1", required = true)
    private Long id;

    @Schema(description = "Nome do atleta ou equipe", example = "João Silva", required = true)
    private String nome;

    @Schema(description = "Tipo de participante (ATLETA ou EQUIPE)", example = "ATLETA", required = true)
    private String tipo;

    @Schema(description = "Nome da equipe (apenas para tipo EQUIPE)", example = "Team Alpha")
    private String nomeEquipe;
}
