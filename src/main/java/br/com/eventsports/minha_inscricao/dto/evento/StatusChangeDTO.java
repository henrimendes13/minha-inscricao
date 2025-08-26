package br.com.eventsports.minha_inscricao.dto.evento;

import br.com.eventsports.minha_inscricao.enums.StatusEvento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para mudança de status de um evento")
public class StatusChangeDTO {

    @NotNull(message = "Novo status é obrigatório")
    @Schema(description = "Novo status do evento", required = true, example = "ABERTO")
    private StatusEvento novoStatus;

    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    @Schema(description = "Motivo da mudança de status", example = "Evento aprovado pela coordenação", maxLength = 500)
    private String motivo;
}