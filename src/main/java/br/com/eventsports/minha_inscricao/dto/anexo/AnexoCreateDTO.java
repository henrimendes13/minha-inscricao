package br.com.eventsports.minha_inscricao.dto.anexo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para criação de um novo anexo")
public class AnexoCreateDTO {

    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    @Schema(description = "Nome original do arquivo", example = "regulamento-evento.pdf", required = true, maxLength = 500)
    private String nomeArquivo;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Schema(description = "Descrição opcional do anexo", example = "Regulamento oficial da competição", maxLength = 1000)
    private String descricao;

}