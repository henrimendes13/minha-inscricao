package br.com.eventsports.minha_inscricao.dto.timeline;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resumo dos dados de uma timeline para listagens")
public class TimelineSummaryDTO {

    @Schema(description = "ID único da timeline", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID do evento ao qual a timeline pertence", example = "1")
    private Long eventoId;

    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nomeEvento;

    @Schema(description = "Descrição completa de todos os dias", 
            example = "Dia 1: 08:00 Check-in | Dia 2: 09:00 Aquecimento")
    private String descricaoCompleta;

    @Schema(description = "Total de dias com cronograma definido", example = "3")
    private Integer totalDiasComDescricao;

    @Schema(description = "Indica se a timeline está vazia", example = "false")
    private Boolean vazia;

    @Schema(description = "Indica se tem descrição do dia um", example = "true")
    private Boolean temDescricaoDiaUm;

    @Schema(description = "Indica se tem descrição do dia dois", example = "true")
    private Boolean temDescricaoDiaDois;

    @Schema(description = "Indica se tem descrição do dia três", example = "false")
    private Boolean temDescricaoDiaTres;

    @Schema(description = "Indica se tem descrição do dia quatro", example = "false")
    private Boolean temDescricaoDiaQuatro;
}