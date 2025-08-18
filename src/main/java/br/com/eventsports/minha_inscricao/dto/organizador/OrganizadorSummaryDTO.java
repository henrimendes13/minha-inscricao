package br.com.eventsports.minha_inscricao.dto.organizador;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resumo do organizador")
public class OrganizadorSummaryDTO {

    @Schema(description = "ID único do organizador", example = "1")
    private Long id;

    @Schema(description = "Nome da empresa organizadora", example = "EventSports LTDA")
    private String nomeEmpresa;

    @Schema(description = "Nome para exibição (empresa ou usuário)")
    private String nomeExibicao;

    @Schema(description = "Indica se o organizador foi verificado", example = "true")
    private Boolean verificado;

    @Schema(description = "Total de eventos criados")
    private Integer totalEventos;

    @Schema(description = "Indica se pode organizar eventos")
    private Boolean podeOrganizarEventos;
}
