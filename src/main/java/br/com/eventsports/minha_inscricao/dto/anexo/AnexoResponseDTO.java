package br.com.eventsports.minha_inscricao.dto.anexo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta completa com dados de um anexo")
public class AnexoResponseDTO {

    @Schema(description = "ID único do anexo", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome original do arquivo", example = "regulamento-evento.pdf")
    private String nomeArquivo;

    @Schema(description = "Descrição opcional do anexo", example = "Regulamento oficial da competição")
    private String descricao;

    @Schema(description = "Caminho onde o arquivo está armazenado", accessMode = Schema.AccessMode.READ_ONLY)
    private String caminhoArquivo;

    @Schema(description = "Tipo MIME do arquivo", example = "application/pdf")
    private String tipoMime;

    @Schema(description = "Tamanho do arquivo em bytes", example = "2048576")
    private Long tamanhoBytes;

    @Schema(description = "Tamanho do arquivo formatado", example = "2.0 MB", accessMode = Schema.AccessMode.READ_ONLY)
    private String tamanhoFormatado;

    @Schema(description = "Extensão do arquivo", example = "pdf")
    private String extensao;

    @Schema(description = "Hash MD5 do arquivo para verificação de integridade")
    private String checksumMd5;

    @Schema(description = "Indica se o anexo está ativo/disponível", example = "true")
    private Boolean ativo;

    @Schema(description = "Nome do evento ao qual o anexo pertence", example = "CrossFit Games 2024")
    private String nomeEvento;

    @Schema(description = "ID do evento ao qual o anexo pertence", example = "1")
    private Long eventoId;

    @Schema(description = "Indica se o arquivo é uma imagem", example = "false", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isImagem;

    @Schema(description = "Indica se o arquivo é um PDF", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean isPdf;

    @Schema(description = "Data de criação do anexo", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}