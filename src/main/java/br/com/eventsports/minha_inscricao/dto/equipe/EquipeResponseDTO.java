package br.com.eventsports.minha_inscricao.dto.equipe;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta completa com dados de uma equipe")
public class EquipeResponseDTO {

    @Schema(description = "ID único da equipe", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome da equipe", example = "Warriors CrossFit")
    private String nome;

    @Schema(description = "ID do evento no qual a equipe irá competir", example = "1")
    private Long eventoId;

    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nomeEvento;

    @Schema(description = "ID da categoria em que a equipe irá competir", example = "1")
    private Long categoriaId;

    @Schema(description = "Nome da categoria", example = "RX Masculino")
    private String nomeCategoria;

    @Schema(description = "ID do atleta capitão", example = "1")
    private Long capitaoId;

    @Schema(description = "Nome do capitão da equipe", example = "João Silva")
    private String nomeCapitao;

    @Schema(description = "Descrição da equipe", example = "Equipe de atletas experientes do CrossFit Warriors")
    private String descricao;

    @Schema(description = "Indica se a equipe está ativa", example = "true")
    private Boolean ativa;

    @Schema(description = "Número de atletas na equipe", example = "4")
    private Integer numeroAtletas;

    @Schema(description = "Lista de nomes dos atletas da equipe")
    private List<String> nomesAtletas;

    @Schema(description = "Indica se a equipe está completa (mínimo 2 atletas)", example = "true")
    private Boolean equipeCompleta;

    @Schema(description = "Indica se pode adicionar mais atletas (máximo 6)", example = "true")
    private Boolean podeAdicionarAtleta;

    @Schema(description = "Indica se todos os atletas aceitaram os termos", example = "true")
    private Boolean todosAtletasAceitaramTermos;

    @Schema(description = "Indica se todos os atletas podem participar", example = "true")
    private Boolean todosAtletasPodemParticipar;

    @Schema(description = "Indica se todos os atletas são compatíveis com a categoria", example = "true")
    private Boolean todosAtletasCompativeisComCategoria;

    @Schema(description = "Indica se a equipe pode se inscrever no evento", example = "true")
    private Boolean podeSeInscrever;

    @Schema(description = "Indica se a equipe tem inscrição no evento", example = "false")
    private Boolean temInscricao;

    @Schema(description = "Indica se a inscrição da equipe está confirmada", example = "false")
    private Boolean inscricaoConfirmada;

    @Schema(description = "Descrição completa da equipe", example = "Warriors CrossFit (4 atletas) - Capitão: João Silva")
    private String descricaoCompleta;

    @Schema(description = "Data de criação da equipe", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
