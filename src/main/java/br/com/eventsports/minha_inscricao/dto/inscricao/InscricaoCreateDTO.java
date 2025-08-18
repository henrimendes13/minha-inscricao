package br.com.eventsports.minha_inscricao.dto.inscricao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação de inscrição")
public class InscricaoCreateDTO {

    @NotNull(message = "Categoria é obrigatória")
    @Schema(description = "ID da categoria", example = "1", required = true)
    private Long categoriaId;

    @NotNull(message = "Aceite dos termos é obrigatório")
    @Schema(description = "Indica se aceitou os termos", example = "true", required = true)
    private Boolean termosAceitos;

    @Size(max = 100, message = "Código de desconto deve ter no máximo 100 caracteres")
    @Schema(description = "Código de desconto aplicado", example = "DESCONTO10")
    private String codigoDesconto;

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Desconto deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Schema(description = "Valor do desconto aplicado", example = "15.00")
    private BigDecimal valorDesconto;
}
