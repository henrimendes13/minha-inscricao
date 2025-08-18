package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscricoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"atletas", "evento", "categoria", "equipe", "pagamento"})
public class InscricaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "inscricao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<AtletaEntity> atletas = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private EventoEntity evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaEntity categoria;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private EquipeEntity equipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusInscricao status = StatusInscricao.PENDENTE;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_inscricao", nullable = false)
    private LocalDateTime dataInscricao;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Column(name = "termos_aceitos", nullable = false)
    private Boolean termosAceitos;

    @Column(name = "codigo_desconto", length = 100)
    private String codigoDesconto;

    @Column(name = "valor_desconto", precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "motivo_cancelamento", length = 200)
    private String motivoCancelamento;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento com Pagamento
    @OneToOne(mappedBy = "inscricao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PagamentoEntity pagamento;

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.dataInscricao == null) {
            this.dataInscricao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusInscricao.PENDENTE;
        }
        
        // Validação: deve ter atletas OU equipe, mas não ambos
        boolean temAtletas = this.atletas != null && !this.atletas.isEmpty();
        boolean temEquipe = this.equipe != null;
        
        if ((!temAtletas && !temEquipe) || (temAtletas && temEquipe)) {
            throw new IllegalStateException("Inscrição deve ter atletas OU uma equipe, mas não ambos");
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public void confirmar() {
        this.status = StatusInscricao.CONFIRMADA;
        this.dataConfirmacao = LocalDateTime.now();
    }

    public void cancelar(String motivo) {
        this.status = StatusInscricao.CANCELADA;
        this.dataCancelamento = LocalDateTime.now();
        this.motivoCancelamento = motivo;
    }

    public void colocarEmListaEspera() {
        this.status = StatusInscricao.LISTA_ESPERA;
    }

    public void recusar(String motivo) {
        this.status = StatusInscricao.RECUSADA;
        this.motivoCancelamento = motivo;
    }

    public void expirar() {
        this.status = StatusInscricao.EXPIRADA;
    }

    public BigDecimal getValorTotal() {
        if (this.valorDesconto != null && this.valorDesconto.compareTo(BigDecimal.ZERO) > 0) {
            return this.valor.subtract(this.valorDesconto);
        }
        return this.valor;
    }

    public boolean temDesconto() {
        return this.valorDesconto != null && this.valorDesconto.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean podeSerCancelada() {
        return this.status.podeCancelar();
    }

    public boolean precisaPagamento() {
        return this.status.podePagar();
    }

    public boolean isAtiva() {
        return this.status.isAtiva();
    }

    public String getNomeAtleta() {
        if (this.atletas != null && !this.atletas.isEmpty()) {
            return this.atletas.get(0).getNomeCompleto();
        }
        return "";
    }

    public String getNomeParticipante() {
        if (this.atletas != null && !this.atletas.isEmpty()) {
            if (this.atletas.size() == 1) {
                return this.atletas.get(0).getNomeCompleto();
            } else {
                return this.atletas.size() + " atletas";
            }
        }
        if (this.equipe != null) {
            return this.equipe.getNome();
        }
        return "";
    }

    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    public String getNomeCategoria() {
        return this.categoria != null ? this.categoria.getNome() : "";
    }

    public String getDescricaoStatus() {
        return this.status != null ? this.status.getDescricao() : "";
    }

    public boolean isPendente() {
        return StatusInscricao.PENDENTE.equals(this.status);
    }

    public boolean isConfirmada() {
        return StatusInscricao.CONFIRMADA.equals(this.status);
    }

    public boolean isCancelada() {
        return StatusInscricao.CANCELADA.equals(this.status);
    }

    public boolean isListaEspera() {
        return StatusInscricao.LISTA_ESPERA.equals(this.status);
    }

    public boolean isInscricaoIndividual() {
        return this.atletas != null && this.atletas.size() == 1 && this.equipe == null;
    }

    public boolean isInscricaoEquipe() {
        return this.equipe != null && (this.atletas == null || this.atletas.isEmpty());
    }

    public boolean isInscricaoMultiplosAtletas() {
        return this.atletas != null && this.atletas.size() > 1 && this.equipe == null;
    }

    public String getTipoInscricao() {
        if (isInscricaoIndividual()) {
            return "Individual";
        }
        if (isInscricaoEquipe()) {
            return "Equipe";
        }
        if (isInscricaoMultiplosAtletas()) {
            return "Múltiplos Atletas";
        }
        return "Indefinido";
    }

    public int getNumeroParticipantes() {
        if (this.atletas != null && !this.atletas.isEmpty()) {
            return this.atletas.size();
        }
        if (isInscricaoEquipe() && this.equipe != null) {
            return this.equipe.getNumeroAtletas();
        }
        return 0;
    }

    // Métodos para gerenciar atletas da inscrição
    public int getTotalAtletas() {
        return this.atletas != null ? this.atletas.size() : 0;
    }

    public void adicionarAtleta(AtletaEntity atleta) {
        if (this.atletas == null) {
            this.atletas = new ArrayList<>();
        }
        
        if (!this.atletas.contains(atleta)) {
            this.atletas.add(atleta);
            atleta.setInscricao(this);
        }
    }

    public void removerAtleta(AtletaEntity atleta) {
        if (this.atletas != null) {
            this.atletas.remove(atleta);
            if (atleta.getInscricao() != null && atleta.getInscricao().equals(this)) {
                atleta.setInscricao(null);
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

    public List<String> getNomesAtletas() {
        return this.atletas != null 
               ? this.atletas.stream()
                   .map(AtletaEntity::getNomeCompleto)
                   .sorted()
                   .toList()
               : new ArrayList<>();
    }
}
