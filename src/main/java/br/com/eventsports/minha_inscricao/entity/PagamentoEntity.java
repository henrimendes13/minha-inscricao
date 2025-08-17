package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.FormaPagamento;
import br.com.eventsports.minha_inscricao.enums.StatusPagamento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados do pagamento de uma inscrição")
public class PagamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do pagamento", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Inscrição é obrigatória")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscricao_id", nullable = false, unique = true)
    @Schema(description = "Inscrição associada ao pagamento")
    private InscricaoEntity inscricao;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Valor do pagamento", example = "150.00", required = true)
    private BigDecimal valor;

    @NotNull(message = "Forma de pagamento é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    @Schema(description = "Forma de pagamento utilizada", example = "PIX", required = true)
    private FormaPagamento formaPagamento;

    @NotNull(message = "Status do pagamento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    @Schema(description = "Status atual do pagamento", example = "PENDENTE", required = true)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Size(max = 100, message = "ID da transação deve ter no máximo 100 caracteres")
    @Column(name = "transacao_id", length = 100, unique = true)
    @Schema(description = "ID da transação no gateway de pagamento", 
            example = "TXN_123456789", accessMode = Schema.AccessMode.READ_ONLY)
    private String transacaoId;

    @Size(max = 100, message = "ID externo deve ter no máximo 100 caracteres")
    @Column(name = "gateway_id", length = 100)
    @Schema(description = "ID do pagamento no gateway externo (PagSeguro, Mercado Pago, etc.)", 
            accessMode = Schema.AccessMode.READ_ONLY)
    private String gatewayId;

    @Column(name = "data_pagamento")
    @Schema(description = "Data/hora em que o pagamento foi realizado", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataPagamento;

    @Column(name = "data_vencimento")
    @Schema(description = "Data de vencimento (para boletos)", example = "2024-12-31T23:59:59")
    private LocalDateTime dataVencimento;

    @Column(name = "data_processamento")
    @Schema(description = "Data/hora do processamento pelo gateway", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime dataProcessamento;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    @Column(name = "observacoes", length = 500)
    @Schema(description = "Observações sobre o pagamento")
    private String observacoes;

    @Size(max = 300, message = "Motivo da recusa deve ter no máximo 300 caracteres")
    @Column(name = "motivo_recusa", length = 300)
    @Schema(description = "Motivo da recusa do pagamento", accessMode = Schema.AccessMode.READ_ONLY)
    private String motivoRecusa;

    @Size(max = 200, message = "Chave PIX deve ter no máximo 200 caracteres")
    @Column(name = "pix_chave", length = 200)
    @Schema(description = "Chave PIX para pagamento", accessMode = Schema.AccessMode.READ_ONLY)
    private String pixChave;

    @Size(max = 500, message = "QR Code PIX deve ter no máximo 500 caracteres")
    @Column(name = "pix_qr_code", length = 500)
    @Schema(description = "Código QR para pagamento PIX", accessMode = Schema.AccessMode.READ_ONLY)
    private String pixQrCode;

    @Size(max = 200, message = "Link do boleto deve ter no máximo 200 caracteres")
    @Column(name = "boleto_url", length = 200)
    @Schema(description = "URL para download/visualização do boleto", accessMode = Schema.AccessMode.READ_ONLY)
    private String boletoUrl;

    @Size(max = 50, message = "Código de barras deve ter no máximo 50 caracteres")
    @Column(name = "boleto_codigo_barras", length = 50)
    @Schema(description = "Código de barras do boleto", accessMode = Schema.AccessMode.READ_ONLY)
    private String boletoCodigoBarras;

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
        if (this.status == null) {
            this.status = StatusPagamento.PENDENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public void aprovar() {
        this.status = StatusPagamento.APROVADO;
        this.dataPagamento = LocalDateTime.now();
        this.dataProcessamento = LocalDateTime.now();
    }

    public void recusar(String motivo) {
        this.status = StatusPagamento.RECUSADO;
        this.motivoRecusa = motivo;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void cancelar() {
        this.status = StatusPagamento.CANCELADO;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void estornar() {
        this.status = StatusPagamento.ESTORNADO;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void expirar() {
        this.status = StatusPagamento.EXPIRADO;
        this.dataProcessamento = LocalDateTime.now();
    }

    public void marcarComoProcessando() {
        this.status = StatusPagamento.PROCESSANDO;
        this.dataProcessamento = LocalDateTime.now();
    }

    public boolean isAprovado() {
        return this.status.isAprovado();
    }

    public boolean isFinalizado() {
        return this.status.isFinalizado();
    }

    public boolean podeSerCancelado() {
        return this.status.podeSerCancelado();
    }

    public boolean podeSerEstornado() {
        return this.status.podeSerEstornado();
    }

    public boolean isPendente() {
        return StatusPagamento.PENDENTE.equals(this.status);
    }

    public boolean isProcessando() {
        return StatusPagamento.PROCESSANDO.equals(this.status);
    }

    public boolean isRecusado() {
        return StatusPagamento.RECUSADO.equals(this.status);
    }

    public boolean temVencimento() {
        return this.dataVencimento != null;
    }

    public boolean isVencido() {
        return temVencimento() && LocalDateTime.now().isAfter(this.dataVencimento);
    }

    public boolean isPix() {
        return FormaPagamento.PIX.equals(this.formaPagamento);
    }

    public boolean isBoleto() {
        return FormaPagamento.BOLETO.equals(this.formaPagamento);
    }

    public boolean isCartao() {
        return FormaPagamento.CARTAO_CREDITO.equals(this.formaPagamento) ||
               FormaPagamento.CARTAO_DEBITO.equals(this.formaPagamento);
    }

    public String getDescricaoFormaPagamento() {
        return this.formaPagamento != null ? this.formaPagamento.getDescricao() : "";
    }

    public String getDescricaoStatus() {
        return this.status != null ? this.status.getDescricao() : "";
    }

    public String getNomeAtleta() {
        return this.inscricao != null && this.inscricao.getAtletas() != null && !this.inscricao.getAtletas().isEmpty()
               ? this.inscricao.getAtletas().get(0).getNomeCompleto() 
               : "";
    }

    public String getNomeEvento() {
        return this.inscricao != null && this.inscricao.getEvento() != null 
               ? this.inscricao.getEvento().getNome() 
               : "";
    }
}
