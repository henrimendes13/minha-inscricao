package br.com.eventsports.minha_inscricao.dto.timeline;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para atualização de uma timeline existente")
public class TimelineUpdateDTO {

    @Size(max = 5000, message = "Descrição do dia um deve ter no máximo 5000 caracteres")
    @Schema(description = "Cronograma do primeiro dia do evento", 
            example = "08:00 - Check-in dos atletas | 09:00 - Aquecimento | 10:00 - Primeira prova")
    private String descricaoDiaUm;

    @Size(max = 5000, message = "Descrição do dia dois deve ter no máximo 5000 caracteres")
    @Schema(description = "Cronograma do segundo dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Segunda prova")
    private String descricaoDiaDois;

    @Size(max = 5000, message = "Descrição do dia três deve ter no máximo 5000 caracteres")
    @Schema(description = "Cronograma do terceiro dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Terceira prova")
    private String descricaoDiaTres;

    @Size(max = 5000, message = "Descrição do dia quatro deve ter no máximo 5000 caracteres")
    @Schema(description = "Cronograma do quarto dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Prova final | 16:00 - Premiação")
    private String descricaoDiaQuatro;
}