package br.com.eventsports.minha_inscricao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboards", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"categoria_id", "workout_id", "equipe_id", "atleta_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "posicao_workout", nullable = false)
    private Integer posicaoWorkout;

    @Column(name = "pontuacao_total", precision = 10, scale = 2)
    private BigDecimal pontuacaoTotal;

    @Column(name = "finalizado", nullable = false)
    @Builder.Default
    private Boolean finalizado = false;

    // Campos de resultado baseados no tipo do workout
    @Column(name = "resultado_reps")
    private Integer resultadoReps;

    @Column(name = "resultado_peso")
    private Double resultadoPeso;

    @Column(name = "resultado_tempo_segundos")
    private Integer resultadoTempoSegundos;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoEntity evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private WorkoutEntity workout;

    // Relacionamento condicional - equipe OU atleta (baseado no tipo da categoria)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private EquipeEntity equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id")
    private AtletaEntity atleta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.finalizado == null) {
            this.finalizado = false;
        }
        
        // Validar que apenas equipe OU atleta está definido
        validarParticipante();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        validarParticipante();
    }

    // Métodos de conveniência

    /**
     * Valida que apenas equipe OU atleta está definido baseado no tipo da categoria
     */
    private void validarParticipante() {
        if (this.categoria != null) {
            if (this.categoria.isEquipe()) {
                if (this.equipe == null) {
                    throw new IllegalStateException("Equipe é obrigatória para categoria do tipo EQUIPE");
                }
                if (this.atleta != null) {
                    throw new IllegalStateException("Atleta deve ser null para categoria do tipo EQUIPE");
                }
            } else if (this.categoria.isIndividual()) {
                if (this.atleta == null) {
                    throw new IllegalStateException("Atleta é obrigatório para categoria do tipo INDIVIDUAL");
                }
                if (this.equipe != null) {
                    throw new IllegalStateException("Equipe deve ser null para categoria do tipo INDIVIDUAL");
                }
            }
        }
    }

    /**
     * Verifica se é uma categoria de equipe
     */
    public boolean isCategoriaEquipe() {
        return this.categoria != null && this.categoria.isEquipe();
    }

    /**
     * Verifica se é uma categoria individual
     */
    public boolean isCategoriaIndividual() {
        return this.categoria != null && this.categoria.isIndividual();
    }

    /**
     * Define o resultado baseado no tipo do workout
     */
    public void definirResultado(Object resultado) {
        if (this.workout != null) {
            switch (this.workout.getTipo()) {
                case REPS:
                    if (resultado instanceof Integer) {
                        this.resultadoReps = (Integer) resultado;
                    }
                    break;
                case PESO:
                    if (resultado instanceof Double) {
                        this.resultadoPeso = (Double) resultado;
                    } else if (resultado instanceof Integer) {
                        this.resultadoPeso = ((Integer) resultado).doubleValue();
                    }
                    break;
                case TEMPO:
                    if (resultado instanceof Integer) {
                        this.resultadoTempoSegundos = (Integer) resultado;
                    }
                    break;
            }
        }
    }

    /**
     * Retorna o resultado principal baseado no tipo do workout
     */
    public Object getResultadoPrincipal() {
        if (this.workout == null) return null;
        
        return switch (this.workout.getTipo()) {
            case REPS -> this.resultadoReps;
            case PESO -> this.resultadoPeso;
            case TEMPO -> this.resultadoTempoSegundos;
        };
    }

    /**
     * Retorna o resultado formatado como string
     */
    public String getResultadoFormatado() {
        if (this.workout == null) return "N/A";
        
        return switch (this.workout.getTipo()) {
            case REPS -> this.resultadoReps != null ? this.resultadoReps + " reps" : "N/A";
            case PESO -> this.resultadoPeso != null ? String.format("%.2f kg", this.resultadoPeso) : "N/A";
            case TEMPO -> formatarTempo(this.resultadoTempoSegundos);
        };
    }

    /**
     * Converte segundos para formato de tempo (mm:ss ou hh:mm:ss)
     */
    public String formatarTempo(Integer segundos) {
        if (segundos == null || segundos <= 0) {
            return "N/A";
        }
        
        int horas = segundos / 3600;
        int minutos = (segundos % 3600) / 60;
        int seg = segundos % 60;
        
        if (horas > 0) {
            return String.format("%d:%02d:%02d", horas, minutos, seg);
        } else {
            return String.format("%d:%02d", minutos, seg);
        }
    }

    /**
     * Verifica se tem resultado definido
     */
    public boolean temResultado() {
        if (this.workout == null) return false;
        
        return switch (this.workout.getTipo()) {
            case REPS -> this.resultadoReps != null && this.resultadoReps > 0;
            case PESO -> this.resultadoPeso != null && this.resultadoPeso > 0;
            case TEMPO -> this.resultadoTempoSegundos != null && this.resultadoTempoSegundos > 0;
        };
    }

    /**
     * Finaliza o resultado
     */
    public void finalizar() {
        this.finalizado = true;
    }

    /**
     * Desqualifica o participante
     */
    public void desqualificar() {
        this.finalizado = false;
        this.posicaoWorkout = null;
        limparResultado();
    }

    /**
     * Limpa o resultado
     */
    public void limparResultado() {
        this.resultadoReps = null;
        this.resultadoPeso = null;
        this.resultadoTempoSegundos = null;
    }

    /**
     * Retorna o nome do participante (equipe ou atleta)
     */
    public String getNomeParticipante() {
        if (isCategoriaEquipe() && this.equipe != null) {
            return this.equipe.getNome();
        } else if (isCategoriaIndividual() && this.atleta != null) {
            return this.atleta.getNomeCompleto();
        }
        return "";
    }

    /**
     * Retorna o nome do evento
     */
    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    /**
     * Retorna o nome da categoria
     */
    public String getNomeCategoria() {
        return this.categoria != null ? this.categoria.getNome() : "";
    }

    /**
     * Retorna o nome do workout
     */
    public String getNomeWorkout() {
        return this.workout != null ? this.workout.getNome() : "";
    }

    /**
     * Retorna descrição completa do resultado
     */
    public String getDescricaoCompleta() {
        StringBuilder sb = new StringBuilder();
        
        if (this.posicaoWorkout != null) {
            sb.append(this.posicaoWorkout).append("º lugar: ");
        }
        
        sb.append(getNomeParticipante());
        sb.append(" - ").append(getResultadoFormatado());
        
        if (this.workout != null) {
            sb.append(" [").append(this.workout.getNome()).append("]");
        }
        
        return sb.toString();
    }

    /**
     * Verifica se está no pódio do workout
     */
    public boolean isPodioWorkout() {
        return this.posicaoWorkout != null && this.posicaoWorkout <= 3;
    }

    /**
     * Retorna a medalha baseada na posição no workout
     */
    public String getMedalhaWorkout() {
        if (this.posicaoWorkout == null) return "";
        
        return switch (this.posicaoWorkout) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "";
        };
    }

    /**
     * Verifica se o participante finalizou o workout
     */
    public boolean isFinalizadoWorkout() {
        return this.finalizado != null && this.finalizado;
    }
}
