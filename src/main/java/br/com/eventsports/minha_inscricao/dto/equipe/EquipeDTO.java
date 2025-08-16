package br.com.eventsports.minha_inscricao.dto.equipe;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"descricao"})
@Schema(description = "Dados básicos de uma equipe")
public class EquipeDTO {

    @Schema(description = "ID único da equipe", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome da equipe", example = "Warriors CrossFit")
    private String nome;

    @Schema(description = "ID do evento no qual a equipe irá competir", example = "1")
    private Long eventoId;

    @Schema(description = "ID da categoria em que a equipe irá competir", example = "1")
    private Long categoriaId;

    @Schema(description = "ID do atleta capitão", example = "1")
    private Long capitaoId;

    @Schema(description = "Descrição da equipe", example = "Equipe de atletas experientes do CrossFit Warriors")
    private String descricao;

    @Schema(description = "Indica se a equipe está ativa", example = "true")
    private Boolean ativa;

    @Schema(description = "Data de criação da equipe", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Constructor customizado para campos essenciais
    public EquipeDTO(String nome, Long eventoId, Long categoriaId, Long capitaoId, String descricao) {
        this.nome = nome;
        this.eventoId = eventoId;
        this.categoriaId = categoriaId;
        this.capitaoId = capitaoId;
        this.descricao = descricao;
        this.ativa = true;
    }
}
