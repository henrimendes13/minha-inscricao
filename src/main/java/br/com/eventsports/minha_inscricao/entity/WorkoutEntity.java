package br.com.eventsports.minha_inscricao.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Exercício ou modalidade de um evento esportivo")
public class WorkoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do workout", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Nome do workout é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Column(name = "nome", nullable = false, length = 200)
    @Schema(description = "Nome do workout", example = "21-15-9 Thrusters/Pull-ups", required = true)
    private String nome;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    @Lob
    @Column(name = "descricao", columnDefinition = "TEXT")
    @Schema(description = "Descrição detalhada do workout", 
            example = "For time: 21-15-9 Thrusters 95/65 Pull-ups")
    private String descricao;

    @Min(value = 1, message = "Ordem deve ser maior que zero")
    @Column(name = "ordem", nullable = false)
    @Schema(description = "Ordem de execução do workout no evento", example = "1", required = true)
    private Integer ordem;

    @PositiveOrZero(message = "Tempo limite deve ser positivo ou zero")
    @Column(name = "tempo_limite_minutos")
    @Schema(description = "Tempo limite em minutos (0 = sem limite)", example = "20")
    private Integer tempoLimiteMinutos;

    @Size(max = 100, message = "Categoria deve ter no máximo 100 caracteres")
    @Column(name = "categoria", length = 100)
    @Schema(description = "Categoria do workout", example = "RX", 
            allowableValues = {"RX", "SCALED", "MASTERS", "TEEN"})
    private String categoria;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    @Schema(description = "Indica se o workout está ativo", example = "true")
    private Boolean ativo = true;

    // Relacionamento com Evento
    @NotNull(message = "Evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @Schema(description = "Evento ao qual o workout pertence")
    private EventoEntity evento;

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
        if (this.ativo == null) {
            this.ativo = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean temTempoLimite() {
        return this.tempoLimiteMinutos != null && this.tempoLimiteMinutos > 0;
    }

    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    public String getDescricaoCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append("Workout ").append(this.ordem).append(": ").append(this.nome);
        if (this.categoria != null) {
            sb.append(" (").append(this.categoria).append(")");
        }
        if (temTempoLimite()) {
            sb.append(" - Tempo limite: ").append(this.tempoLimiteMinutos).append(" min");
        }
        return sb.toString();
    }
}
