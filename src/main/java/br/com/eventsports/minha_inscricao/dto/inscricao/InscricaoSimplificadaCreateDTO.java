package br.com.eventsports.minha_inscricao.dto.inscricao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação simplificada de inscrição (aceita email e nome em vez de IDs)")
public class InscricaoSimplificadaCreateDTO {

    @NotBlank(message = "Email do usuário é obrigatório")
    @Email(message = "Email inválido")
    @Schema(description = "Email do usuário que está fazendo a inscrição", example = "admin@admin.com", required = true)
    private String usuarioEmail;

    @NotNull(message = "ID do evento é obrigatório")
    @Schema(description = "ID do evento", example = "1", required = true)
    private Long eventoId;

    @NotNull(message = "ID da categoria é obrigatório")
    @Schema(description = "ID da categoria", example = "1", required = true)
    private Long categoriaId;

    // Para inscrição individual
    @Schema(description = "Nome do atleta (para inscrição individual)", example = "Henrique Mendes Cruz")
    private String atletaNome;

    // Para inscrição em equipe
    @Schema(description = "Nome da equipe (para inscrição de equipe)", example = "Team Alpha")
    private String nomeEquipe;

    @Schema(description = "Nomes dos atletas da equipe")
    private List<String> atletasNomes;

    @Schema(description = "Índice do capitão na lista de atletas (0-based)", example = "0")
    private Integer capitaoIndex;

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
