package br.com.eventsports.minha_inscricao.dto.equipe;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resumo de uma equipe para listagens")
public class EquipeSummaryDTO {

    @Schema(description = "ID único da equipe", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome da equipe", example = "Warriors CrossFit")
    private String nome;

    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nomeEvento;

    @Schema(description = "Nome da categoria", example = "RX Masculino")
    private String nomeCategoria;

    @Schema(description = "Nome do capitão da equipe", example = "João Silva")
    private String nomeCapitao;

    @Schema(description = "Indica se a equipe está ativa", example = "true")
    private Boolean ativa;

    @Schema(description = "Número de atletas na equipe", example = "4")
    private Integer numeroAtletas;

    @Schema(description = "Lista de nomes dos atletas da equipe")
    private List<String> nomesAtletas;

    @Schema(description = "Indica se a equipe está completa (mínimo 2 atletas)", example = "true")
    private Boolean equipeCompleta;

    @Schema(description = "Indica se a equipe pode se inscrever no evento", example = "true")
    private Boolean podeSeInscrever;

    @Schema(description = "Indica se a equipe tem inscrição no evento", example = "false")
    private Boolean temInscricao;

    @Schema(description = "Indica se a inscrição da equipe está confirmada", example = "false")
    private Boolean inscricaoConfirmada;

    @Schema(description = "Data de criação da equipe", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
