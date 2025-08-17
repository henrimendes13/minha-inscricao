package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.StatusEvento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"descricao", "inscricoes", "workouts", "leaderboards", "timeline", "anexos", "atletas"})
public class EventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "data_inicio_evento", nullable = false)
    private LocalDateTime dataInicioDoEvento;

    @Column(name = "data_fim_evento", nullable = false)
    private LocalDateTime dataFimDoEvento;

    @Lob
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Novos campos para integração com o sistema completo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id")
    private OrganizadorEntity organizador;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    @Builder.Default
    private StatusEvento status = StatusEvento.RASCUNHO;

    // Relacionamentos
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CategoriaEntity> categorias = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InscricaoEntity> inscricoes = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkoutEntity> workouts = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LeaderboardEntity> leaderboards = new ArrayList<>();

    @OneToOne(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TimelineEntity timeline;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AnexoEntity> anexos = new ArrayList<>();

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AtletaEntity> atletas = new ArrayList<>();

    // Constructor customizado para campos essenciais
    public EventoEntity(String nome, LocalDateTime dataInicioDoEvento, LocalDateTime dataFimDoEvento, String descricao) {
        this.nome = nome;
        this.dataInicioDoEvento = dataInicioDoEvento;
        this.dataFimDoEvento = dataFimDoEvento;
        this.descricao = descricao;
        this.status = StatusEvento.RASCUNHO;
    }

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusEvento.RASCUNHO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public void publicar() {
        this.status = StatusEvento.ABERTO;
    }

    public void encerrarInscricoes() {
        this.status = StatusEvento.INSCRICOES_ENCERRADAS;
    }

    public void iniciar() {
        this.status = StatusEvento.EM_ANDAMENTO;
    }

    public void finalizar() {
        this.status = StatusEvento.FINALIZADO;
    }

    public void cancelar() {
        this.status = StatusEvento.CANCELADO;
    }

    public void adiar() {
        this.status = StatusEvento.ADIADO;
    }

    public boolean podeReceberInscricoes() {
        return this.status != null && this.status.podeReceberInscricoes();
    }

    public boolean podeSerEditado() {
        return this.status != null && this.status.podeSerEditado();
    }

    public String getNomeOrganizador() {
        return this.organizador != null ? this.organizador.getNomeExibicao() : "";
    }

    public int getTotalCategorias() {
        return this.categorias != null ? this.categorias.size() : 0;
    }

    public int getTotalInscricoes() {
        return this.inscricoes != null ? this.inscricoes.size() : 0;
    }

    public long getInscricoesAtivas() {
        return this.inscricoes != null 
               ? this.inscricoes.stream()
                   .filter(inscricao -> inscricao.getStatus().isAtiva())
                   .count()
               : 0;
    }

    public int getTotalWorkouts() {
        return this.workouts != null ? this.workouts.size() : 0;
    }

    public int getTotalWorkoutsAtivos() {
        return this.workouts != null 
               ? (int) this.workouts.stream()
                   .filter(workout -> workout.getAtivo())
                   .count()
               : 0;
    }

    public int getTotalLeaderboards() {
        return this.leaderboards != null ? this.leaderboards.size() : 0;
    }

    public long getAtletasFinalizados() {
        return this.leaderboards != null 
               ? this.leaderboards.stream()
                   .filter(leaderboard -> leaderboard.getFinalizado())
                   .count()
               : 0;
    }

    public List<AtletaEntity> getAtletasInscritos() {
        return this.inscricoes != null 
               ? this.inscricoes.stream()
                   .filter(inscricao -> inscricao.getStatus().isAtiva())
                   .flatMap(inscricao -> inscricao.getAtletas().stream())
                   .toList()
               : new ArrayList<>();
    }

    public int getTotalAnexos() {
        return this.anexos != null ? this.anexos.size() : 0;
    }

    public int getTotalAnexosAtivos() {
        return this.anexos != null 
               ? (int) this.anexos.stream()
                   .filter(anexo -> anexo.isAtivo())
                   .count()
               : 0;
    }

    public boolean temTimeline() {
        return this.timeline != null && !this.timeline.isVazia();
    }

    public boolean temAnexos() {
        return this.anexos != null && !this.anexos.isEmpty();
    }

    public String getDescricaoTimeline() {
        return this.timeline != null ? this.timeline.getDescricaoCompleta() : "";
    }

    public List<AnexoEntity> getAnexosAtivos() {
        return this.anexos != null 
               ? this.anexos.stream()
                   .filter(anexo -> anexo.isAtivo())
                   .sorted((a1, a2) -> {
                       if (a1.getOrdem() != null && a2.getOrdem() != null) {
                           return a1.getOrdem().compareTo(a2.getOrdem());
                       }
                       return a1.getDescricao().compareTo(a2.getDescricao());
                   })
                   .toList()
               : new ArrayList<>();
    }

    public String getDescricaoStatus() {
        return this.status != null ? this.status.getDescricao() : "";
    }

    // Métodos para gerenciar atletas do evento
    public int getTotalAtletas() {
        return this.atletas != null ? this.atletas.size() : 0;
    }

    public List<AtletaEntity> getAtletasAtivos() {
        return this.atletas != null 
               ? this.atletas.stream()
                   .filter(AtletaEntity::podeParticipar)
                   .sorted((a1, a2) -> a1.getNomeCompleto().compareTo(a2.getNomeCompleto()))
                   .toList()
               : new ArrayList<>();
    }

    public int getTotalAtletasAtivos() {
        return getAtletasAtivos().size();
    }

    public void adicionarAtleta(AtletaEntity atleta) {
        if (this.atletas == null) {
            this.atletas = new ArrayList<>();
        }
        
        if (!this.atletas.contains(atleta)) {
            this.atletas.add(atleta);
            atleta.setEvento(this);
        }
    }

    public void removerAtleta(AtletaEntity atleta) {
        if (this.atletas != null) {
            this.atletas.remove(atleta);
            if (atleta.getEvento() != null && atleta.getEvento().equals(this)) {
                atleta.setEvento(null);
            }
        }
    }

    public boolean contemAtleta(AtletaEntity atleta) {
        return this.atletas != null && this.atletas.contains(atleta);
    }

    public boolean contemAtletaPorId(Long atletaId) {
        return this.atletas != null && 
               this.atletas.stream().anyMatch(atleta -> atleta.getId().equals(atletaId));
    }

    public AtletaEntity buscarAtletaPorId(Long atletaId) {
        return this.atletas != null 
               ? this.atletas.stream()
                   .filter(atleta -> atleta.getId().equals(atletaId))
                   .findFirst()
                   .orElse(null)
               : null;
    }

    public List<String> getNomesAtletas() {
        return this.atletas != null 
               ? this.atletas.stream()
                   .map(AtletaEntity::getNomeCompleto)
                   .sorted()
                   .toList()
               : new ArrayList<>();
    }

    public boolean temAtletas() {
        return this.atletas != null && !this.atletas.isEmpty();
    }
}
