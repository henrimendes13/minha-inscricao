package br.com.eventsports.minha_inscricao.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para criação de um novo evento esportivo")
public class EventoCreateDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Schema(description = "Nome do evento", example = "CrossFit Games 2024", required = true, maxLength = 200)
    private String nome;

    @NotNull(message = "Data de início é obrigatória")
    @Future(message = "Data de início do evento deve ser no futuro")
    @Schema(description = "Data e hora de início do evento", example = "2024-12-15T10:00:00", required = true, type = "string", format = "date-time")
    private LocalDateTime dataInicioDoEvento;

    @NotNull(message = "Data de fim é obrigatória")
    @Future(message = "Data de fim do evento deve ser no futuro")
    @Schema(description = "Data e hora de fim do evento", example = "2024-12-15T18:00:00", required = true, type = "string", format = "date-time")
    private LocalDateTime dataFimDoEvento;
    
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Schema(description = "Descrição detalhada do evento", example = "Competição de CrossFit com atletas de elite", maxLength = 5000)
    private String descricao;
}
