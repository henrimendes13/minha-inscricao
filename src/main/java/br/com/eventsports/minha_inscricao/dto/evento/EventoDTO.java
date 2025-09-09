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

    @Schema(description = "Data e hora de início do evento", example = "2024-12-15T10:00:00", type = "string", format = "date-time")
    private LocalDateTime dataInicioDoEvento;

    @Schema(description = "Data e hora de fim do evento", example = "2024-12-15T18:00:00", type = "string", format = "date-time")
    private LocalDateTime dataFimDoEvento;
    
    @Schema(description = "Descrição detalhada do evento", example = "Competição de CrossFit com atletas de elite")
    private String descricao;

    @Schema(description = "Cidade onde o evento será realizado", example = "São Paulo")
    private String cidade;

    @Schema(description = "Estado onde o evento será realizado", example = "SP")
    private String estado;

    @Schema(description = "Endereço completo do evento", example = "Rua das Flores, 123 - Centro")
    private String endereco;
    
    @Schema(description = "Data de criação do evento", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Constructor customizado para campos essenciais
    public EventoDTO(String nome, LocalDateTime dataInicioDoEvento, LocalDateTime dataFimDoEvento, String descricao) {
        this.nome = nome;
        this.dataInicioDoEvento = dataInicioDoEvento;
        this.dataFimDoEvento = dataFimDoEvento;
        this.descricao = descricao;
    }

    // Constructor customizado incluindo campos de localização
    public EventoDTO(String nome, LocalDateTime dataInicioDoEvento, LocalDateTime dataFimDoEvento, String descricao,
                     String cidade, String estado, String endereco) {
        this.nome = nome;
        this.dataInicioDoEvento = dataInicioDoEvento;
        this.dataFimDoEvento = dataFimDoEvento;
        this.descricao = descricao;
        this.cidade = cidade;
        this.estado = estado;
        this.endereco = endereco;
    }
}
