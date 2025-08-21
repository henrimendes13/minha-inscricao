package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoEntity evento;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", length = 300)
    private String descricao;

    @Column(name = "idade_minima")
    private Integer idadeMinima;

    @Column(name = "idade_maxima")
    private Integer idadeMaxima;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 20)
    private Genero genero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_participacao", nullable = false, length = 20)
    @Builder.Default
    private TipoParticipacao tipoParticipacao = TipoParticipacao.INDIVIDUAL;

    @Column(name = "quantidade_atletas_por_equipe")
    private Integer quantidadeDeAtletasPorEquipe;

    @Column(name = "valor_inscricao", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorInscricao;

    @Builder.Default
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamentos
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InscricaoEntity> inscricoes = new ArrayList<>();

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EquipeEntity> equipes = new ArrayList<>();

    @ManyToMany(mappedBy = "categorias", fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkoutEntity> workouts = new ArrayList<>();

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LeaderboardEntity> leaderboards = new ArrayList<>();

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.ativa == null) {
            this.ativa = true;
        }
        if (this.tipoParticipacao == null) {
            this.tipoParticipacao = TipoParticipacao.INDIVIDUAL;
        }
        // Define quantidade de atletas padrão baseada no tipo de participação
        if (this.quantidadeDeAtletasPorEquipe == null) {
            this.quantidadeDeAtletasPorEquipe = isIndividual() ? 1 : 3; // Padrão: 1 para individual, 3 para equipe
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public boolean atletaPodeParticipar(UsuarioEntity atleta) {
        if (!this.ativa) {
            return false;
        }

        // Verificar gênero
        if (this.genero != null && !this.genero.equals(atleta.getGenero())) {
            return false;
        }

        int idade = atleta.getIdade();

        // Verificar idade mínima
        if (this.idadeMinima != null && idade < this.idadeMinima) {
            return false;
        }

        // Verificar idade máxima
        if (this.idadeMaxima != null && idade > this.idadeMaxima) {
            return false;
        }

        return true;
    }

    public long getNumeroInscricoesAtivas() {
        return this.inscricoes != null 
               ? this.inscricoes.stream()
                   .filter(inscricao -> inscricao.getStatus().isAtiva())
                   .count()
               : 0;
    }

    public String getDescricaoCompleta() {
        StringBuilder sb = new StringBuilder(this.nome);
        
        if (this.genero != null) {
            sb.append(" - ").append(this.genero.getDescricao());
        }
        
        if (this.idadeMinima != null || this.idadeMaxima != null) {
            sb.append(" (");
            if (this.idadeMinima != null) {
                sb.append(this.idadeMinima).append("+");
            }
            if (this.idadeMaxima != null) {
                if (this.idadeMinima != null) {
                    sb.append(" até ");
                }
                sb.append(this.idadeMaxima).append(" anos");
            }
            sb.append(")");
        }
        
        return sb.toString();
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public boolean isIndividual() {
        return TipoParticipacao.INDIVIDUAL.equals(this.tipoParticipacao);
    }

    public boolean isEquipe() {
        return TipoParticipacao.EQUIPE.equals(this.tipoParticipacao);
    }

    public boolean equipePodeParticipar(EquipeEntity equipe) {
        if (!this.ativa || !isEquipe()) {
            return false;
        }

        // Verificar se a equipe tem a quantidade correta de atletas
        if (!equipeTemQuantidadeCorretaDeAtletas(equipe)) {
            return false;
        }

        // Verificar se todos os atletas da equipe podem participar
        return equipe.getAtletas().stream()
                .allMatch(this::atletaPodeParticipar);
    }

    public boolean equipeTemQuantidadeCorretaDeAtletas(EquipeEntity equipe) {
        if (this.quantidadeDeAtletasPorEquipe == null) {
            return true; // Se não foi definido, aceita qualquer quantidade
        }
        
        return equipe.getNumeroAtletas() == this.quantidadeDeAtletasPorEquipe;
    }

    public boolean listaAtletasTemQuantidadeCorreta(List<Long> atletasIds) {
        if (this.quantidadeDeAtletasPorEquipe == null) {
            return true; // Se não foi definido, aceita qualquer quantidade
        }
        
        return atletasIds != null && atletasIds.size() == this.quantidadeDeAtletasPorEquipe;
    }

    public long getNumeroEquipesAtivas() {
        return this.equipes != null 
               ? this.equipes.stream()
                   .filter(EquipeEntity::getAtiva)
                   .count()
               : 0;
    }

    public int getTotalEquipes() {
        return this.equipes != null ? this.equipes.size() : 0;
    }

    // Métodos de conveniência para workouts
    public void adicionarWorkout(WorkoutEntity workout) {
        if (this.workouts == null) {
            this.workouts = new ArrayList<>();
        }
        if (!this.workouts.contains(workout)) {
            this.workouts.add(workout);
            workout.getCategorias().add(this);
        }
    }

    public void removerWorkout(WorkoutEntity workout) {
        if (this.workouts != null) {
            this.workouts.remove(workout);
            workout.getCategorias().remove(this);
        }
    }

    public boolean temWorkout(WorkoutEntity workout) {
        return this.workouts != null && this.workouts.contains(workout);
    }

    public long getNumeroWorkoutsAtivos() {
        return this.workouts != null 
               ? this.workouts.stream()
                   .filter(WorkoutEntity::getAtivo)
                   .count()
               : 0;
    }

    public String getNomesWorkouts() {
        if (this.workouts == null || this.workouts.isEmpty()) {
            return "Sem workouts";
        }
        return this.workouts.stream()
                .map(WorkoutEntity::getNome)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Sem workouts");
    }

    public int getQuantidadeWorkouts() {
        return this.workouts != null ? this.workouts.size() : 0;
    }

    // Métodos de conveniência para leaderboards
    
    /**
     * Calcula a pontuação total de uma equipe (soma das posições em todos os workouts)
     */
    public Integer calcularPontuacaoTotalEquipe(EquipeEntity equipe) {
        if (this.leaderboards == null || !isEquipe()) {
            return null;
        }
        
        return this.leaderboards.stream()
                .filter(lb -> equipe.equals(lb.getEquipe()) && lb.getPosicaoWorkout() != null)
                .mapToInt(LeaderboardEntity::getPosicaoWorkout)
                .sum();
    }

    /**
     * Calcula a pontuação total de um atleta (soma das posições em todos os workouts)
     */
    public Integer calcularPontuacaoTotalAtleta(UsuarioEntity atleta) {
        if (this.leaderboards == null || !isIndividual()) {
            return null;
        }
        
        return this.leaderboards.stream()
                .filter(lb -> atleta.equals(lb.getAtleta()) && lb.getPosicaoWorkout() != null)
                .mapToInt(LeaderboardEntity::getPosicaoWorkout)
                .sum();
    }

    /**
     * Retorna o leaderboard final ordenado por pontuação total (menor pontuação primeiro)
     */
    public List<Object[]> getLeaderboardFinal() {
        if (this.leaderboards == null) {
            return new ArrayList<>();
        }

        if (isEquipe()) {
            return this.leaderboards.stream()
                    .filter(lb -> lb.getEquipe() != null)
                    .collect(Collectors.groupingBy(LeaderboardEntity::getEquipe))
                    .entrySet().stream()
                    .map(entry -> {
                        EquipeEntity equipe = entry.getKey();
                        Integer pontuacaoTotal = calcularPontuacaoTotalEquipe(equipe);
                        return new Object[]{equipe, pontuacaoTotal};
                    })
                    .filter(entry -> entry[1] != null)
                    .sorted((a, b) -> Integer.compare((Integer) a[1], (Integer) b[1]))
                    .collect(Collectors.toList());
        } else {
            return this.leaderboards.stream()
                    .filter(lb -> lb.getAtleta() != null)
                    .collect(Collectors.groupingBy(LeaderboardEntity::getAtleta))
                    .entrySet().stream()
                    .map(entry -> {
                        UsuarioEntity atleta = entry.getKey();
                        Integer pontuacaoTotal = calcularPontuacaoTotalAtleta(atleta);
                        return new Object[]{atleta, pontuacaoTotal};
                    })
                    .filter(entry -> entry[1] != null)
                    .sorted((a, b) -> Integer.compare((Integer) a[1], (Integer) b[1]))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Retorna quantos participantes finalizaram todos os workouts
     */
    public long getParticipantesFinalizados() {
        if (this.leaderboards == null) {
            return 0;
        }

        if (isEquipe()) {
            return this.leaderboards.stream()
                    .filter(lb -> lb.getEquipe() != null)
                    .collect(Collectors.groupingBy(LeaderboardEntity::getEquipe))
                    .entrySet().stream()
                    .filter(entry -> {
                        List<LeaderboardEntity> resultados = entry.getValue();
                        return resultados.stream().allMatch(LeaderboardEntity::isFinalizadoWorkout);
                    })
                    .count();
        } else {
            return this.leaderboards.stream()
                    .filter(lb -> lb.getAtleta() != null)
                    .collect(Collectors.groupingBy(LeaderboardEntity::getAtleta))
                    .entrySet().stream()
                    .filter(entry -> {
                        List<LeaderboardEntity> resultados = entry.getValue();
                        return resultados.stream().allMatch(LeaderboardEntity::isFinalizadoWorkout);
                    })
                    .count();
        }
    }
}
