package br.com.eventsports.minha_inscricao.dto.timeline;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta completa com dados de uma timeline de evento")
public class TimelineResponseDTO {

    @Schema(description = "ID único da timeline", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID do evento ao qual a timeline pertence", example = "1")
    private Long eventoId;

    @Schema(description = "Nome do evento", example = "CrossFit Games 2024")
    private String nomeEvento;

    @Schema(description = "Cronograma do primeiro dia do evento", 
            example = "08:00 - Check-in dos atletas | 09:00 - Aquecimento | 10:00 - Primeira prova")
    private String descricaoDiaUm;

    @Schema(description = "Cronograma do segundo dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Segunda prova")
    private String descricaoDiaDois;

    @Schema(description = "Cronograma do terceiro dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Terceira prova")
    private String descricaoDiaTres;

    @Schema(description = "Cronograma do quarto dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Prova final | 16:00 - Premiação")
    private String descricaoDiaQuatro;

    @Schema(description = "Descrição completa de todos os dias", 
            example = "Dia 1: 08:00 Check-in | Dia 2: 09:00 Aquecimento")
    private String descricaoCompleta;

    @Schema(description = "Total de dias com cronograma definido", example = "3")
    private Integer totalDiasComDescricao;

    @Schema(description = "Indica se a timeline está vazia", example = "false")
    private Boolean vazia;

    @Schema(description = "Indica se tem descrição do dia um", example = "true")
    private Boolean temDescricaoDiaUm;

    @Schema(description = "Indica se tem descrição do dia dois", example = "true")
    private Boolean temDescricaoDiaDois;

    @Schema(description = "Indica se tem descrição do dia três", example = "false")
    private Boolean temDescricaoDiaTres;

    @Schema(description = "Indica se tem descrição do dia quatro", example = "false")
    private Boolean temDescricaoDiaQuatro;

    @Schema(description = "Data de criação da timeline", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}