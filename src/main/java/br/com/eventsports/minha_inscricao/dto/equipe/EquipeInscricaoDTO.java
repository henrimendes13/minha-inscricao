package br.com.eventsports.minha_inscricao.dto.equipe;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

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
    @Schema(description = "Lista de IDs dos atletas que farão parte da equipe (1-6 atletas)", 
            example = "[1, 2, 3]", 
            required = true)
    private List<Long> atletasIds;

    @Schema(description = "ID do atleta que será capitão (opcional - se não informado, será o primeiro da lista)", 
            example = "1")
    private Long capitaoId;
}
