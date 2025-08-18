package br.com.eventsports.minha_inscricao.dto.inscricao;

import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO resumido da inscrição para listagens")
public class InscricaoSummaryDTO {

    @Schema(description = "ID único da inscrição", example = "1")
    private Long id;

    @Schema(description = "Nome do evento", example = "Campeonato de CrossFit 2024")
    private String nomeEvento;

    @Schema(description = "Nome da categoria", example = "Masters 35+")
    private String nomeCategoria;

    @Schema(description = "Nome da equipe", example = "Team Alpha")
    private String nomeEquipe;

    @Schema(description = "Status da inscrição", example = "PENDENTE")
    private StatusInscricao status;

    @Schema(description = "Descrição do status", example = "Aguardando pagamento")
    private String descricaoStatus;

    @Schema(description = "Valor total da inscrição", example = "135.00")
    private BigDecimal valorTotal;

    @Schema(description = "Data/hora da inscrição", example = "2024-01-15T10:30:00")
    private LocalDateTime dataInscricao;

    @Schema(description = "Data/hora da confirmação", example = "2024-01-15T12:00:00")
    private LocalDateTime dataConfirmacao;

    @Schema(description = "Tipo de inscrição", example = "Individual")
    private String tipoInscricao;

    @Schema(description = "Número de participantes", example = "1")
    private Integer numeroParticipantes;

    @Schema(description = "Nome do participante principal", example = "João Silva Santos")
    private String nomeParticipante;

    @Schema(description = "Se está ativa", example = "true")
    private Boolean ativa;

    @Schema(description = "Se pode ser cancelada", example = "true")
    private Boolean podeSerCancelada;

    @Schema(description = "Se precisa de pagamento", example = "true")
    private Boolean precisaPagamento;
}
