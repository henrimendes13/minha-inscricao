package br.com.eventsports.minha_inscricao.dto.inscricao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação manual de inscrição pelo administrador")
public class InscricaoCreateDTO {

    @NotNull(message = "ID do usuário é obrigatório")
    @Schema(description = "ID do usuário que está fazendo a inscrição", example = "1", required = true)
    private Long usuarioInscricaoId;

    @NotNull(message = "ID do evento é obrigatório")
    @Schema(description = "ID do evento", example = "1", required = true)
    private Long eventoId;

    @NotNull(message = "ID da categoria é obrigatório")
    @Schema(description = "ID da categoria", example = "1", required = true)
    private Long categoriaId;

    @Schema(description = "ID do atleta (para inscrição individual)", example = "1")
    private Long atletaId;

    @Schema(description = "ID da equipe (para inscrição de equipe)", example = "1")
    private Long equipeId;

    @NotNull(message = "Valor da inscrição é obrigatório")
    @Schema(description = "Valor da inscrição", example = "150.00", required = true)
    private BigDecimal valor;

    @Schema(description = "Código de desconto aplicado", example = "DESCONTO10")
    private String codigoDesconto;

    @Schema(description = "Valor do desconto aplicado", example = "15.00")
    private BigDecimal valorDesconto;

    @NotNull(message = "Aceitação dos termos é obrigatória")
    @Schema(description = "Se aceitou os termos", example = "true", required = true)
    private Boolean termosAceitos;

    @Schema(description = "Observações adicionais")
    private String observacoes;
}
