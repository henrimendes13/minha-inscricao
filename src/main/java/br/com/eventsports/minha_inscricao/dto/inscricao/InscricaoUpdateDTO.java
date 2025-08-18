package br.com.eventsports.minha_inscricao.dto.inscricao;

import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para atualização de inscrição")
public class InscricaoUpdateDTO {

    @Schema(description = "ID da categoria", example = "1")
    private Long categoriaId;

    @Schema(description = "Status da inscrição", example = "CONFIRMADA")
    private StatusInscricao status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Valor deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Schema(description = "Valor da inscrição", example = "150.00")
    private BigDecimal valor;

    @Schema(description = "Se aceitou os termos", example = "true")
    private Boolean termosAceitos;

    @Size(max = 100, message = "Código de desconto deve ter no máximo 100 caracteres")
    @Schema(description = "Código de desconto aplicado", example = "DESCONTO10")
    private String codigoDesconto;

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Desconto deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Schema(description = "Valor do desconto aplicado", example = "15.00")
    private BigDecimal valorDesconto;

    @Size(max = 200, message = "Motivo do cancelamento deve ter no máximo 200 caracteres")
    @Schema(description = "Motivo do cancelamento")
    private String motivoCancelamento;
}
