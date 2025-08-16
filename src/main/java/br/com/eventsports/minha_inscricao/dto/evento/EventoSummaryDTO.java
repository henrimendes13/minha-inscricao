package br.com.eventsports.minha_inscricao.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resumo de um evento esportivo para listagens")
public class EventoSummaryDTO {

    @Schema(description = "ID único do evento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nome;

    @Schema(description = "Data e hora de início do evento", example = "2024-12-15T10:00:00", type = "string", format = "date-time")
    private LocalDateTime dataInicioDoEvento;

    @Schema(description = "Data e hora de fim do evento", example = "2024-12-15T18:00:00", type = "string", format = "date-time")
    private LocalDateTime dataFimDoEvento;

    @Schema(description = "Status atual do evento", example = "ABERTO")
    private String status;

    @Schema(description = "Descrição do status", example = "Evento aberto para inscrições")
    private String descricaoStatus;

    @Schema(description = "Nome do organizador responsável", example = "João Silva")
    private String nomeOrganizador;

    @Schema(description = "Total de categorias do evento", example = "5")
    private Integer totalCategorias;

    @Schema(description = "Total de inscrições ativas", example = "20")
    private Long inscricoesAtivas;

    @Schema(description = "Indica se o evento pode receber inscrições", example = "true")
    private Boolean podeReceberInscricoes;
    
    @Schema(description = "Data de criação do evento", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
}
