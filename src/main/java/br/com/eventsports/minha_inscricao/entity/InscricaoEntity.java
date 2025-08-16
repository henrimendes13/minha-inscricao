package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscricoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Inscrição individual ou de equipe em um evento específico")
public class InscricaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da inscrição", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atleta_id")
    @Schema(description = "Atleta que está se inscrevendo (null para inscrições de equipe)")
    private AtletaEntity atleta;

    @NotNull(message = "Evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @Schema(description = "Evento no qual o atleta está se inscrevendo")
    private EventoEntity evento;

    @NotNull(message = "Categoria é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @Schema(description = "Categoria em que o atleta/equipe está competindo")
    private CategoriaEntity categoria;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    @Schema(description = "Equipe que está se inscrevendo (null para inscrições individuais)")
    private EquipeEntity equipe;

    @NotNull(message = "Status da inscrição é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    @Schema(description = "Status atual da inscrição", example = "PENDENTE", required = true)
    private StatusInscricao status = StatusInscricao.PENDENTE;

    @NotNull(message = "Valor da inscrição é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Valor pago pela inscrição", example = "150.00", required = true)
    private BigDecimal valor;

    @Column(name = "data_inscricao", nullable = false)
    @Schema(description = "Data/hora da inscrição", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataInscricao;

    @Column(name = "data_confirmacao")
    @Schema(description = "Data/hora da confirmação do pagamento", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataConfirmacao;

    @Column(name = "data_cancelamento")
    @Schema(description = "Data/hora do cancelamento", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataCancelamento;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Column(name = "observacoes", length = 500)
    @Schema(description = "Observações sobre a inscrição", 
            example = "Atleta iniciante, primeira competição")
    private String observacoes;

    @NotNull(message = "Aceite dos termos é obrigatório")
    @Column(name = "termos_aceitos", nullable = false)
    @Schema(description = "Indica se o atleta aceitou os termos", example = "true", required = true)
    private Boolean termosAceitos;

    @Size(max = 100, message = "Código de desconto deve ter no máximo 100 caracteres")
    @Column(name = "codigo_desconto", length = 100)
    @Schema(description = "Código de desconto aplicado", example = "DESCONTO10")
    private String codigoDesconto;

    @DecimalMin(value = "0.0", inclusive = true, message = "Desconto deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Desconto deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "valor_desconto", precision = 10, scale = 2)
    @Schema(description = "Valor do desconto aplicado", example = "15.00")
    private BigDecimal valorDesconto;

    @Size(max = 200, message = "Motivo do cancelamento deve ter no máximo 200 caracteres")
    @Column(name = "motivo_cancelamento", length = 200)
    @Schema(description = "Motivo do cancelamento da inscrição")
    private String motivoCancelamento;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Data de criação", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Relacionamento com Pagamento
    @OneToOne(mappedBy = "inscricao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "Pagamento associado à inscrição", accessMode = Schema.AccessMode.READ_ONLY)
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
        
        // Validação: deve ter atleta OU equipe, mas não ambos
        if ((this.atleta == null && this.equipe == null) || 
            (this.atleta != null && this.equipe != null)) {
            throw new IllegalStateException("Inscrição deve ter exatamente um atleta OU uma equipe");
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
        return this.atleta != null ? this.atleta.getNomeCompleto() : "";
    }

    public String getNomeParticipante() {
        if (this.atleta != null) {
            return this.atleta.getNomeCompleto();
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
        return this.atleta != null && this.equipe == null;
    }

    public boolean isInscricaoEquipe() {
        return this.equipe != null && this.atleta == null;
    }

    public String getTipoInscricao() {
        if (isInscricaoIndividual()) {
            return "Individual";
        }
        if (isInscricaoEquipe()) {
            return "Equipe";
        }
        return "Indefinido";
    }

    public int getNumeroParticipantes() {
        if (isInscricaoIndividual()) {
            return 1;
        }
        if (isInscricaoEquipe() && this.equipe != null) {
            return this.equipe.getNumeroAtletas();
        }
        return 0;
    }
}
