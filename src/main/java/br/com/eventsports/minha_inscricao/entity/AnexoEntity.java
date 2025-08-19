package br.com.eventsports.minha_inscricao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "anexos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnexoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_arquivo", nullable = false, length = 500)
    private String nomeArquivo;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Column(name = "caminho_arquivo", nullable = false, length = 1000)
    private String caminhoArquivo;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Column(name = "extensao", length = 10)
    private String extensao;

    @Column(name = "checksum_md5", length = 32)
    private String checksumMd5;

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
        if (this.nomeArquivo != null && this.extensao == null) {
            this.extensao = extrairExtensao(this.nomeArquivo);
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

    public boolean isAtivo() {
        return Boolean.TRUE.equals(this.ativo);
    }

    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    /**
     * Retorna o tamanho formatado em unidades legíveis
     */
    public String getTamanhoFormatado() {
        if (this.tamanhoBytes == null) return "0 B";
        
        long bytes = this.tamanhoBytes;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    /**
     * Verifica se é um arquivo de imagem
     */
    public boolean isImagem() {
        if (this.tipoMime == null) return false;
        return this.tipoMime.startsWith("image/");
    }

    /**
     * Verifica se é um PDF
     */
    public boolean isPdf() {
        return "application/pdf".equals(this.tipoMime);
    }

    /**
     * Extrai a extensão do nome do arquivo
     */
    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) return "";
        return nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Gera um nome único para o arquivo baseado no ID e timestamp
     */
    public String gerarNomeArquivoUnico() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String ext = this.extensao != null ? "." + this.extensao : "";
        return String.format("anexo_%d_%s%s", this.id != null ? this.id : 0, timestamp, ext);
    }
}
