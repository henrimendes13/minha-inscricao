package br.com.eventsports.minha_inscricao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organizadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private UsuarioEntity usuario;

    @Column(name = "nome_empresa", length = 100)
    private String nomeEmpresa;

    @Column(name = "cnpj", length = 18, unique = true)
    private String cnpj;

    @Column(name = "telefone", length = 15)
    private String telefone;

    @Column(name = "endereco", length = 200)
    private String endereco;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "site", length = 100)
    private String site;

    @Builder.Default
    @Column(name = "verificado", nullable = false)
    private Boolean verificado = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamentos
    @OneToMany(mappedBy = "organizador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventoEntity> eventos = new ArrayList<>();

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.verificado == null) {
            this.verificado = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public void verificar() {
        this.verificado = true;
    }

    public void removerVerificacao() {
        this.verificado = false;
    }

    public boolean podeOrganizarEventos() {
        return this.usuario != null && 
               this.usuario.getAtivo() && 
               this.verificado;
    }

    public String getNomeExibicao() {
        return this.nomeEmpresa != null && !this.nomeEmpresa.trim().isEmpty() 
               ? this.nomeEmpresa 
               : this.usuario.getNome();
    }

    public int getTotalEventos() {
        return this.eventos != null ? this.eventos.size() : 0;
    }
}