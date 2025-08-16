package br.com.eventsports.minha_inscricao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipes", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"nome", "evento_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"descricao", "atletas", "inscricao"})
public class EquipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoEntity evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capitao_id", nullable = false)
    private AtletaEntity capitao;

    @Column(name = "descricao", length = 300)
    private String descricao;

    @Builder.Default
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamentos
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "equipe_atletas",
        joinColumns = @JoinColumn(name = "equipe_id"),
        inverseJoinColumns = @JoinColumn(name = "atleta_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"equipe_id", "atleta_id"})
    )
    @Builder.Default
    private List<AtletaEntity> atletas = new ArrayList<>();

    @OneToOne(mappedBy = "equipe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InscricaoEntity inscricao;

    // Constructor customizado para campos essenciais
    public EquipeEntity(String nome, EventoEntity evento, CategoriaEntity categoria, AtletaEntity capitao, String descricao) {
        this.nome = nome;
        this.evento = evento;
        this.categoria = categoria;
        this.capitao = capitao;
        this.descricao = descricao;
        this.ativa = true;
    }

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.ativa == null) {
            this.ativa = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public void adicionarAtleta(AtletaEntity atleta) {
        if (this.atletas == null) {
            this.atletas = new ArrayList<>();
        }
        
        if (this.atletas.size() >= 6) {
            throw new IllegalStateException("Equipe já possui o número máximo de atletas (6)");
        }
        
        if (!this.atletas.contains(atleta)) {
            this.atletas.add(atleta);
        }
    }

    public void removerAtleta(AtletaEntity atleta) {
        if (this.atletas != null) {
            this.atletas.remove(atleta);
            
            // Se o capitão foi removido, escolhe um novo capitão
            if (this.capitao != null && this.capitao.equals(atleta) && !this.atletas.isEmpty()) {
                this.capitao = this.atletas.get(0);
            }
        }
    }

    public void definirCapitao(AtletaEntity atleta) {
        if (this.atletas == null || !this.atletas.contains(atleta)) {
            throw new IllegalArgumentException("Atleta deve ser membro da equipe para ser capitão");
        }
        this.capitao = atleta;
    }

    public boolean isEquipeCompleta() {
        return getNumeroAtletas() >= 2;
    }

    public boolean podeAdicionarAtleta() {
        return getNumeroAtletas() < 6;
    }

    public int getNumeroAtletas() {
        return this.atletas != null ? this.atletas.size() : 0;
    }

    public boolean contemAtleta(AtletaEntity atleta) {
        return this.atletas != null && this.atletas.contains(atleta);
    }

    public boolean todosAtletasAceitaramTermos() {
        if (this.atletas == null || this.atletas.isEmpty()) {
            return false;
        }
        
        return this.atletas.stream()
                .allMatch(atleta -> atleta.getAceitaTermos() != null && atleta.getAceitaTermos());
    }

    public boolean todosAtletasPodemParticipar() {
        if (this.atletas == null || this.atletas.isEmpty()) {
            return false;
        }
        
        return this.atletas.stream().allMatch(AtletaEntity::podeParticipar);
    }

    public boolean todosAtletasCompativeisComCategoria() {
        if (this.categoria == null || this.atletas == null || this.atletas.isEmpty()) {
            return false;
        }
        
        return this.atletas.stream()
                .allMatch(atleta -> this.categoria.atletaPodeParticipar(atleta));
    }

    public boolean podeSeInscrever() {
        return this.ativa && 
               isEquipeCompleta() && 
               todosAtletasAceitaramTermos() && 
               todosAtletasPodemParticipar() && 
               todosAtletasCompativeisComCategoria();
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public String getNomeCapitao() {
        return this.capitao != null ? this.capitao.getNomeCompleto() : "";
    }

    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    public String getNomeCategoria() {
        return this.categoria != null ? this.categoria.getNome() : "";
    }

    public boolean temInscricao() {
        return this.inscricao != null;
    }

    public boolean inscricaoConfirmada() {
        return this.inscricao != null && this.inscricao.isConfirmada();
    }

    public List<String> getNomesAtletas() {
        if (this.atletas == null || this.atletas.isEmpty()) {
            return new ArrayList<>();
        }
        
        return this.atletas.stream()
                .map(AtletaEntity::getNomeCompleto)
                .sorted()
                .toList();
    }

    public String getDescricaoCompleta() {
        StringBuilder sb = new StringBuilder(this.nome);
        sb.append(" (").append(getNumeroAtletas()).append(" atletas)");
        
        if (this.capitao != null) {
            sb.append(" - Capitão: ").append(this.capitao.getNomeCompleto());
        }
        
        return sb.toString();
    }
}
