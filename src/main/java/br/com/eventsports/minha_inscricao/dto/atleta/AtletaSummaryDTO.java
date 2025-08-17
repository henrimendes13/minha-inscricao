package br.com.eventsports.minha_inscricao.dto.atleta;

import br.com.eventsports.minha_inscricao.enums.Genero;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO resumido do atleta para listagens")
public class AtletaSummaryDTO {

    @Schema(description = "ID único do atleta", example = "1")
    private Long id;

    @Schema(description = "Nome completo do atleta", example = "João Silva Santos")
    private String nome;

    @Schema(description = "Data de nascimento do atleta", example = "1990-05-15")
    private LocalDate dataNascimento;

    @Schema(description = "Gênero do atleta", example = "MASCULINO")
    private Genero genero;

    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String telefone;

    @Schema(description = "Indica se aceitou os termos", example = "true")
    private Boolean aceitaTermos;

    // Informações calculadas
    @Schema(description = "Idade atual do atleta", example = "34")
    private Integer idade;

    @Schema(description = "Se pode participar", example = "true")
    private Boolean podeParticipar;

    // Informações resumidas de relacionamentos
    @Schema(description = "Nome do evento vinculado", example = "Campeonato de CrossFit 2024")
    private String nomeEvento;

    @Schema(description = "Status da inscrição", example = "CONFIRMADA")
    private String statusInscricao;

    @Schema(description = "Nome da equipe", example = "Team Alpha")
    private String nomeEquipe;
}
