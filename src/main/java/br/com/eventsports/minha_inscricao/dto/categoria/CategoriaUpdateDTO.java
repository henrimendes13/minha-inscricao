package br.com.eventsports.minha_inscricao.dto.categoria;

import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualização de uma categoria")
public class CategoriaUpdateDTO {

    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 100, message = "Nome da categoria deve ter no máximo 100 caracteres")
    @Schema(description = "Nome da categoria", example = "Masculino Elite", required = true, maxLength = 100)
    private String nome;

    @Size(max = 300, message = "Descrição deve ter no máximo 300 caracteres")
    @Schema(description = "Descrição detalhada da categoria", 
            example = "Categoria para atletas masculinos experientes, acima de 25 anos", 
            maxLength = 300)
    private String descricao;

    @Min(value = 16, message = "Idade mínima deve ser pelo menos 16 anos")
    @Max(value = 80, message = "Idade mínima não pode ser maior que 80 anos")
    @Schema(description = "Idade mínima para participar", example = "18")
    private Integer idadeMinima;

    @Min(value = 16, message = "Idade máxima deve ser pelo menos 16 anos")
    @Max(value = 100, message = "Idade máxima não pode ser maior que 100 anos")
    @Schema(description = "Idade máxima para participar", example = "35")
    private Integer idadeMaxima;

    @Schema(description = "Gênero específico da categoria (null = misto)", example = "MASCULINO")
    private Genero genero;

    @NotNull(message = "Tipo de participação é obrigatório")
    @Schema(description = "Tipo de participação: INDIVIDUAL ou EQUIPE", example = "INDIVIDUAL", required = true)
    private TipoParticipacao tipoParticipacao;

    @Min(value = 1, message = "Quantidade de atletas por equipe deve ser pelo menos 1")
    @Max(value = 6, message = "Quantidade de atletas por equipe não pode ser maior que 6")
    @Schema(description = "Quantidade exata de atletas que devem compor uma equipe nesta categoria. " +
                         "Para categorias individuais, normalmente é 1. Para equipes, pode ser 2-6. " +
                         "ATENÇÃO: Não é possível alterar se a categoria já possuir equipes ou inscrições.", 
            example = "3")
    private Integer quantidadeDeAtletasPorEquipe;

    @Schema(description = "Indica se a categoria está ativa para inscrições", example = "true")
    private Boolean ativa;
}
