package br.com.eventsports.minha_inscricao.dto.anexo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resumo com informações básicas de um anexo")
public class AnexoSummaryDTO {

    @Schema(description = "ID único do anexo", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome original do arquivo", example = "regulamento-evento.pdf")
    private String nomeArquivo;

    @Schema(description = "Descrição opcional do anexo", example = "Regulamento oficial da competição")
    private String descricao;

    @Schema(description = "Extensão do arquivo", example = "pdf")
    private String extensao;

    @Schema(description = "Tamanho do arquivo formatado", example = "2.0 MB", accessMode = Schema.AccessMode.READ_ONLY)
    private String tamanhoFormatado;

    @Schema(description = "Indica se o anexo está ativo/disponível", example = "true")
    private Boolean ativo;

    @Schema(description = "Tipo MIME do arquivo", example = "application/pdf")
    private String tipoMime;
}