package br.com.eventsports.minha_inscricao.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "anexos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Anexo ou documento de um evento esportivo")
public class AnexoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do anexo", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Nome do arquivo é obrigatório")
    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    @Column(name = "nome_arquivo", nullable = false, length = 500)
    @Schema(description = "Nome original do arquivo", example = "regulamento-evento.pdf", required = true)
    private String nomeArquivo;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Column(name = "descricao", length = 1000)
    @Schema(description = "Descrição opcional do anexo", example = "Regulamento oficial da competição")
    private String descricao;

    @NotBlank(message = "Caminho do arquivo é obrigatório")
    @Column(name = "caminho_arquivo", nullable = false, length = 1000)
    @Schema(description = "Caminho onde o arquivo está armazenado", accessMode = Schema.AccessMode.READ_ONLY)
    private String caminhoArquivo;

    @NotBlank(message = "Tipo MIME é obrigatório")
    @Column(name = "tipo_mime", nullable = false, length = 100)
    @Schema(description = "Tipo MIME do arquivo", example = "application/pdf")
    private String tipoMime;

    @NotNull(message = "Tamanho do arquivo é obrigatório")
    @Min(value = 1, message = "Tamanho deve ser maior que 0")
    @Column(name = "tamanho_bytes", nullable = false)
    @Schema(description = "Tamanho do arquivo em bytes", example = "2048576")
    private Long tamanhoBytes;

    @Size(max = 10, message = "Extensão deve ter no máximo 10 caracteres")
    @Column(name = "extensao", length = 10)
    @Schema(description = "Extensão do arquivo", example = "pdf")
    private String extensao;

    @Column(name = "checksum_md5", length = 32)
    @Schema(description = "Hash MD5 do arquivo para verificação de integridade")
    private String checksumMd5;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    @Schema(description = "Indica se o anexo está ativo/disponível", example = "true")
    private Boolean ativo = true;

    // Relacionamento com Evento
    @NotNull(message = "Evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @Schema(description = "Evento ao qual o anexo pertence")
    private EventoEntity evento;

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
