package br.com.eventsports.minha_inscricao.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"descricao"})
@Schema(description = "Dados básicos de um evento esportivo")
public class EventoDTO {

    @Schema(description = "ID único do evento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nome;

    @Schema(description = "Data e hora do evento", example = "2024-12-15T10:00:00", type = "string", format = "date-time")
    private LocalDateTime data;
    
    @Schema(description = "Descrição detalhada do evento", example = "Competição de CrossFit com atletas de elite")
    private String descricao;
    
    @Schema(description = "Data de criação do evento", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Constructor customizado para campos essenciais
    public EventoDTO(String nome, LocalDateTime data, String descricao) {
        this.nome = nome;
        this.data = data;
        this.descricao = descricao;
    }
}
