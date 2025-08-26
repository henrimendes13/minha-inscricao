package br.com.eventsports.minha_inscricao.dto.equipe;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaCreateDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para criação de equipe durante inscrição em evento")
public class EquipeInscricaoDTO {

    @NotBlank(message = "Nome da equipe é obrigatório")
    @Size(min = 3, max = 100, message = "Nome da equipe deve ter entre 3 e 100 caracteres")
    @Schema(description = "Nome da equipe", example = "Warriors CrossFit", required = true, maxLength = 100, minLength = 3)
    private String nome;

    @NotNull(message = "Categoria é obrigatória")
    @Schema(description = "ID da categoria em que a equipe irá competir", example = "1", required = true)
    private Long categoriaId;

    @NotEmpty(message = "Lista de atletas não pode estar vazia")
    @Size(min = 1, max = 6, message = "Uma equipe deve ter entre 1 e 6 atletas")
    @Valid
    @Schema(description = "Lista completa dos dados dos atletas que farão parte da equipe (1-6 atletas)", 
            required = true)
    private List<AtletaCreateDTO> atletas;

    @Schema(description = "CPF do atleta que será capitão (opcional - se não informado, será o primeiro da lista)", 
            example = "123.456.789-00")
    private String capitaoCpf;

    @Schema(description = "Valor personalizado da inscrição (opcional - se não informado, será usado o valor da categoria)", 
            example = "50.00")
    private BigDecimal valorInscricao;

    @Schema(description = "Código de desconto aplicável (opcional)", 
            example = "DESCONTO10")
    private String codigoDesconto;

    @NotNull(message = "Aceitação dos termos e condições é obrigatória")
    @Schema(description = "Confirmação de que a equipe aceita os termos e condições do evento", 
            example = "true", required = true)
    private Boolean termosAceitos;
}
