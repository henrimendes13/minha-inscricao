package br.com.eventsports.minha_inscricao.dto.atleta;

import br.com.eventsports.minha_inscricao.enums.Genero;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resposta completa do atleta")
public class AtletaResponseDTO {

    @Schema(description = "ID único do atleta", example = "1")
    private Long id;

    @Schema(description = "Nome completo do atleta", example = "João Silva Santos")
    private String nome;

    @Schema(description = "CPF do atleta", example = "123.456.789-00")
    private String cpf;

    @Schema(description = "Data de nascimento do atleta", example = "1990-05-15")
    private LocalDate dataNascimento;

    @Schema(description = "Gênero do atleta", example = "MASCULINO")
    private Genero genero;

    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String telefone;

    @Schema(description = "Nome do contato de emergência", example = "Maria Silva Santos")
    private String emergenciaNome;

    @Schema(description = "Telefone do contato de emergência", example = "(11) 88888-8888")
    private String emergenciaTelefone;

    @Schema(description = "Observações médicas importantes", 
            example = "Alérgico a anti-inflamatórios. Problema no joelho direito.")
    private String observacoesMedicas;

    @Schema(description = "Endereço completo", example = "Rua das Palmeiras, 456 - Vila Nova - São Paulo/SP")
    private String endereco;

    @Schema(description = "Indica se aceitou os termos", example = "true")
    private Boolean aceitaTermos;

    @Schema(description = "Data de criação", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    // Informações calculadas
    @Schema(description = "Idade atual do atleta", example = "34")
    private Integer idade;

    @Schema(description = "Se o atleta é maior de idade", example = "true")
    private Boolean maiorIdade;

    @Schema(description = "Se tem contato de emergência cadastrado", example = "true")
    private Boolean temContatoEmergencia;

    @Schema(description = "Se pode participar", example = "true")
    private Boolean podeParticipar;

    // Informações de relacionamentos
    @Schema(description = "ID do evento vinculado", example = "1")
    private Long eventoId;

    @Schema(description = "Nome do evento vinculado", example = "Campeonato de CrossFit 2024")
    private String nomeEvento;

    @Schema(description = "ID da inscrição", example = "1")
    private Long inscricaoId;

    @Schema(description = "Status da inscrição", example = "CONFIRMADA")
    private String statusInscricao;

    @Schema(description = "Nome da categoria da inscrição", example = "Masters 35+")
    private String nomeCategoriaInscricao;

    @Schema(description = "ID da equipe", example = "1")
    private Long equipeId;

    @Schema(description = "Nome da equipe", example = "Team Alpha")
    private String nomeEquipe;
}
