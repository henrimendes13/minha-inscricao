package br.com.eventsports.minha_inscricao.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboards", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"evento_id", "atleta_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Classificação de um atleta em um evento")
public class LeaderboardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da classificação", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Posição é obrigatória")
    @Positive(message = "Posição deve ser um número positivo")
    @Column(name = "posicao", nullable = false)
    @Schema(description = "Posição do atleta na classificação", example = "1", required = true)
    private Integer posicao;

    @NotNull(message = "Pontuação é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Pontuação deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Pontuação deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "pontuacao", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Pontuação total do atleta", example = "480.50", required = true)
    private BigDecimal pontuacao;

    @PositiveOrZero(message = "Tempo total deve ser positivo ou zero")
    @Column(name = "tempo_total_segundos")
    @Schema(description = "Tempo total em segundos (para eventos cronometrados)", example = "1800")
    private Integer tempoTotalSegundos;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Column(name = "observacoes", length = 500)
    @Schema(description = "Observações sobre a performance", 
            example = "Excelente performance, recorde pessoal")
    private String observacoes;

    @Column(name = "finalizado", nullable = false)
    @Builder.Default
    @Schema(description = "Indica se o atleta finalizou todos os workouts", example = "true")
    private Boolean finalizado = false;

    @Size(max = 50, message = "Categoria deve ter no máximo 50 caracteres")
    @Column(name = "categoria", length = 50)
    @Schema(description = "Categoria em que o atleta competiu", example = "RX")
    private String categoria;

    // Relacionamentos
    @NotNull(message = "Evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @Schema(description = "Evento da classificação")
    private EventoEntity evento;

    @NotNull(message = "Atleta é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id", nullable = false)
    @Schema(description = "Atleta classificado")
    private AtletaEntity atleta;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Data de criação", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.finalizado == null) {
            this.finalizado = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public void finalizar() {
        this.finalizado = true;
    }

    public void desqualificar() {
        this.finalizado = false;
        this.pontuacao = BigDecimal.ZERO;
    }

    public boolean temTempo() {
        return this.tempoTotalSegundos != null && this.tempoTotalSegundos > 0;
    }

    public String getTempoFormatado() {
        if (!temTempo()) {
            return "N/A";
        }
        
        int horas = this.tempoTotalSegundos / 3600;
        int minutos = (this.tempoTotalSegundos % 3600) / 60;
        int segundos = this.tempoTotalSegundos % 60;
        
        if (horas > 0) {
            return String.format("%d:%02d:%02d", horas, minutos, segundos);
        } else {
            return String.format("%d:%02d", minutos, segundos);
        }
    }

    public String getNomeAtleta() {
        return this.atleta != null ? this.atleta.getNomeCompleto() : "";
    }

    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    public String getDescricaoCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.posicao).append("º lugar: ");
        sb.append(getNomeAtleta());
        sb.append(" - ").append(this.pontuacao).append(" pts");
        
        if (temTempo()) {
            sb.append(" (").append(getTempoFormatado()).append(")");
        }
        
        if (this.categoria != null) {
            sb.append(" [").append(this.categoria).append("]");
        }
        
        return sb.toString();
    }

    public boolean isPodio() {
        return this.posicao != null && this.posicao <= 3;
    }

    public String getMedalha() {
        if (this.posicao == null) return "";
        
        return switch (this.posicao) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "";
        };
    }
}
