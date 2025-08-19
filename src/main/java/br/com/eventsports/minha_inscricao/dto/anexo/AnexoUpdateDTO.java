package br.com.eventsports.minha_inscricao.dto.anexo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualização de um anexo")
public class AnexoUpdateDTO {

    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    @Schema(description = "Nome original do arquivo", example = "regulamento-evento-atualizado.pdf", required = true, maxLength = 500)
    private String nomeArquivo;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Schema(description = "Descrição opcional do anexo", example = "Regulamento oficial da competição - versão 2", maxLength = 1000)
    private String descricao;

    @Schema(description = "Indica se o anexo está ativo/disponível", example = "true")
    private Boolean ativo;
}