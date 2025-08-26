package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.TipoWorkout;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @Builder.Default
    private TipoWorkout tipo = TipoWorkout.REPS;


    // Relacionamento Many-to-Many com Categorias
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "workout_categorias",
        joinColumns = @JoinColumn(name = "workout_id"),
        inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @Builder.Default
    private List<CategoriaEntity> categorias = new ArrayList<>();

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    // Relacionamento com Evento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoEntity evento;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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

    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    // Métodos de conveniência para categorias
    public void adicionarCategoria(CategoriaEntity categoria) {
        if (this.categorias == null) {
            this.categorias = new ArrayList<>();
        }
        if (!this.categorias.contains(categoria)) {
            this.categorias.add(categoria);
            categoria.getWorkouts().add(this);
        }
    }

    public void removerCategoria(CategoriaEntity categoria) {
        if (this.categorias != null) {
            this.categorias.remove(categoria);
            categoria.getWorkouts().remove(this);
        }
    }

    public boolean temCategoria(CategoriaEntity categoria) {
        return this.categorias != null && this.categorias.contains(categoria);
    }

    public String getNomesCategorias() {
        if (this.categorias == null || this.categorias.isEmpty()) {
            return "Sem categorias";
        }
        return this.categorias.stream()
                .map(CategoriaEntity::getNome)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sem categorias");
    }

    public int getQuantidadeCategorias() {
        return this.categorias != null ? this.categorias.size() : 0;
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
     * Converte string de tempo (mm:ss ou hh:mm:ss) para segundos
     */
    public static Integer converterTempoParaSegundos(String tempo) {
        if (tempo == null || tempo.trim().isEmpty()) {
            return null;
        }
        
        String[] partes = tempo.trim().split(":");
        
        try {
            if (partes.length == 2) {
                // Formato mm:ss
                int minutos = Integer.parseInt(partes[0]);
                int segundos = Integer.parseInt(partes[1]);
                return minutos * 60 + segundos;
            } else if (partes.length == 3) {
                // Formato hh:mm:ss
                int horas = Integer.parseInt(partes[0]);
                int minutos = Integer.parseInt(partes[1]);
                int segundos = Integer.parseInt(partes[2]);
                return horas * 3600 + minutos * 60 + segundos;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de tempo inválido. Use mm:ss ou hh:mm:ss");
        }
        
        throw new IllegalArgumentException("Formato de tempo inválido. Use mm:ss ou hh:mm:ss");
    }
    
    
    /**
     * Retorna a unidade de medida baseada no tipo
     */
    public String getUnidadeMedida() {
        return switch (this.tipo) {
            case REPS -> "repetições";
            case PESO -> "kg";
            case TEMPO -> "tempo";
        };
    }
}
