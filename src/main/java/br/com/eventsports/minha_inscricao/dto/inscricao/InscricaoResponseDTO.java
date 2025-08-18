package br.com.eventsports.minha_inscricao.dto.inscricao;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaSummaryDTO;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resposta completa da inscrição")
public class InscricaoResponseDTO {

    @Schema(description = "ID único da inscrição", example = "1")
    private Long id;

    @Schema(description = "Lista de atletas vinculados à inscrição")
    private List<AtletaSummaryDTO> atletas;

    @Schema(description = "ID do evento", example = "1")
    private Long eventoId;

    @Schema(description = "Nome do evento", example = "Campeonato de CrossFit 2024")
    private String nomeEvento;

    @Schema(description = "ID da categoria", example = "1")
    private Long categoriaId;

    @Schema(description = "Nome da categoria", example = "Masters 35+")
    private String nomeCategoria;

    @Schema(description = "ID da equipe", example = "1")
    private Long equipeId;

    @Schema(description = "Nome da equipe", example = "Team Alpha")
    private String nomeEquipe;

    @Schema(description = "Status da inscrição", example = "PENDENTE")
    private StatusInscricao status;

    @Schema(description = "Descrição do status", example = "Aguardando pagamento")
    private String descricaoStatus;

    @Schema(description = "Valor da inscrição", example = "150.00")
    private BigDecimal valor;

    @Schema(description = "Data/hora da inscrição", example = "2024-01-15T10:30:00")
    private LocalDateTime dataInscricao;

    @Schema(description = "Data/hora da confirmação", example = "2024-01-15T12:00:00")
    private LocalDateTime dataConfirmacao;

    @Schema(description = "Data/hora do cancelamento", example = "2024-01-16T09:00:00")
    private LocalDateTime dataCancelamento;

    @Schema(description = "Se aceitou os termos", example = "true")
    private Boolean termosAceitos;

    @Schema(description = "Código de desconto aplicado", example = "DESCONTO10")
    private String codigoDesconto;

    @Schema(description = "Valor do desconto aplicado", example = "15.00")
    private BigDecimal valorDesconto;

    @Schema(description = "Motivo do cancelamento")
    private String motivoCancelamento;

    @Schema(description = "Data de criação", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    // Informações calculadas
    @Schema(description = "Valor total após desconto", example = "135.00")
    private BigDecimal valorTotal;

    @Schema(description = "Se tem desconto aplicado", example = "true")
    private Boolean temDesconto;

    @Schema(description = "Se pode ser cancelada", example = "true")
    private Boolean podeSerCancelada;

    @Schema(description = "Se precisa de pagamento", example = "true")
    private Boolean precisaPagamento;

    @Schema(description = "Se está ativa", example = "true")
    private Boolean ativa;

    @Schema(description = "Tipo de inscrição", example = "Individual")
    private String tipoInscricao;

    @Schema(description = "Número de participantes", example = "1")
    private Integer numeroParticipantes;

    @Schema(description = "Nome do participante principal", example = "João Silva Santos")
    private String nomeParticipante;

    @Schema(description = "Se tem pagamento associado", example = "true")
    private Boolean temPagamento;
}
