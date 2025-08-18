package br.com.eventsports.minha_inscricao.dto.inscricao;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação de inscrição com equipe e atletas")
public class InscricaoComEquipeCreateDTO {

    @NotNull(message = "Categoria é obrigatória")
    @Schema(description = "ID da categoria", example = "1", required = true)
    private Long categoriaId;

    @NotBlank(message = "Nome da equipe é obrigatório")
    @Size(max = 100, message = "Nome da equipe deve ter no máximo 100 caracteres")
    @Schema(description = "Nome da equipe", example = "Team Alpha", required = true)
    private String nomeEquipe;

    @Size(max = 300, message = "Descrição da equipe deve ter no máximo 300 caracteres")
    @Schema(description = "Descrição da equipe", example = "Equipe de atletas experientes")
    private String descricaoEquipe;

    @NotNull(message = "Lista de atletas é obrigatória")
    @Size(min = 1, message = "Pelo menos um atleta deve ser informado")
    @Valid
    @Schema(description = "Lista de atletas da equipe", required = true)
    private List<AtletaCreateDTO> atletas;

    @Schema(description = "Índice do atleta que será o capitão (0-based)", example = "0")
    private Integer indiceCapitao;

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
