package br.com.eventsports.minha_inscricao.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta completa com dados de um evento esportivo")
public class EventoResponseDTO {

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

    @Schema(description = "Informações sobre inscrições", example = "Inscrições abertas até 31/12/2024. Taxa: R$ 150,00")
    private String inscricao;
    
    @Schema(description = "Cronograma do evento", example = "Check-in 08:00, Aquecimento 09:00, Competição 10:00")
    private String timeline;
    
    @Schema(description = "Classificação/ranking dos participantes", example = "1º Lugar: João Silva - 480pts")
    private String leaderboard;
    
    @Schema(description = "Descrição dos exercícios/modalidades", example = "WOD 1: 21-15-9 Thrusters/Pull-ups")
    private String workouts;
    
    @Schema(description = "Arquivos anexos", example = "Regulamento.pdf, Termos.pdf")
    private String anexos;
    
    @Schema(description = "Informações das equipes participantes", example = "Team Alpha, Team Beta")
    private String equipes;
    
    @Schema(description = "Lista de atletas", example = "João Silva, Maria Santos, Pedro Costa")
    private String atletas;
    
    @Schema(description = "Descrição detalhada do evento", example = "Competição de CrossFit com atletas de elite")
    private String descricao;

    @Schema(description = "Cidade onde o evento será realizado", example = "São Paulo")
    private String cidade;

    @Schema(description = "Estado onde o evento será realizado", example = "SP")
    private String estado;

    @Schema(description = "Endereço completo do evento", example = "Rua das Flores, 123 - Centro")
    private String endereco;

    @Schema(description = "Total de categorias do evento", example = "5")
    private Integer totalCategorias;

    @Schema(description = "Total de inscrições no evento", example = "25")
    private Integer totalInscricoes;

    @Schema(description = "Total de inscrições ativas", example = "20")
    private Long inscricoesAtivas;

    @Schema(description = "Indica se o evento pode receber inscrições", example = "true")
    private Boolean podeReceberInscricoes;

    @Schema(description = "Indica se o evento pode ser editado", example = "true")
    private Boolean podeSerEditado;
    
    @Schema(description = "Data de criação do evento", example = "2024-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;
    
    @Schema(description = "Data da última atualização", example = "2024-01-16T14:20:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;
}
