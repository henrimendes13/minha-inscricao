package br.com.eventsports.minha_inscricao.dto.categoria;

import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta completa com dados de uma categoria")
public class CategoriaResponseDTO {

    @Schema(description = "ID único da categoria", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID do evento ao qual a categoria pertence", example = "1")
    private Long eventoId;

    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nomeEvento;

    @Schema(description = "Nome da categoria", example = "Masculino Elite")
    private String nome;

    @Schema(description = "Descrição detalhada da categoria", 
            example = "Categoria para atletas masculinos experientes, acima de 25 anos")
    private String descricao;

    @Schema(description = "Idade mínima para participar", example = "18")
    private Integer idadeMinima;

    @Schema(description = "Idade máxima para participar", example = "35")
    private Integer idadeMaxima;

    @Schema(description = "Gênero específico da categoria", example = "MASCULINO")
    private Genero genero;

    @Schema(description = "Tipo de participação: INDIVIDUAL ou EQUIPE", example = "INDIVIDUAL")
    private TipoParticipacao tipoParticipacao;

    @Schema(description = "Quantidade exata de atletas que devem compor uma equipe nesta categoria", example = "3")
    private Integer quantidadeDeAtletasPorEquipe;

    @Schema(description = "Valor da inscrição para esta categoria", example = "150.00")
    private BigDecimal valorInscricao;

    @Schema(description = "Indica se a categoria está ativa para inscrições", example = "true")
    private Boolean ativa;

    @Schema(description = "Número de inscrições ativas nesta categoria", example = "25", accessMode = Schema.AccessMode.READ_ONLY)
    private Long numeroInscricoesAtivas;

    @Schema(description = "Número de equipes ativas nesta categoria", example = "8", accessMode = Schema.AccessMode.READ_ONLY)
    private Long numeroEquipesAtivas;

    @Schema(description = "Total de equipes nesta categoria", example = "10", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer totalEquipes;

    @Schema(description = "Descrição completa da categoria", example = "Masculino Elite - Masculino (18+ até 35 anos)", accessMode = Schema.AccessMode.READ_ONLY)
    private String descricaoCompleta;

    @Schema(description = "Data de criação da categoria", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
