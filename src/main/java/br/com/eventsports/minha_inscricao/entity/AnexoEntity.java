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

    @NotBlank(message = "Descrição do anexo é obrigatória")
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Column(name = "descricao", nullable = false, length = 1000)
    @Schema(description = "Descrição ou nome do anexo", 
            example = "Regulamento da competição, Termos de participação, Manual do atleta",
            required = true)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    @Schema(description = "Indica se o anexo está ativo/disponível", example = "true")
    private Boolean ativo = true;

    @Min(value = 1, message = "Ordem deve ser maior que zero")
    @Column(name = "ordem")
    @Schema(description = "Ordem de exibição do anexo", example = "1")
    private Integer ordem;

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

    public String getDescricaoComOrdem() {
        if (this.ordem != null) {
            return this.ordem + ". " + this.descricao;
        }
        return this.descricao;
    }

    public boolean temOrdem() {
        return this.ordem != null && this.ordem > 0;
    }

    /**
     * Retorna true se o anexo parece ser um arquivo (contém extensão)
     */
    public boolean pareceArquivo() {
        if (this.descricao == null) return false;
        
        String desc = this.descricao.toLowerCase();
        return desc.contains(".pdf") || desc.contains(".doc") || 
               desc.contains(".xlsx") || desc.contains(".png") || 
               desc.contains(".jpg") || desc.contains(".jpeg") ||
               desc.contains(".zip") || desc.contains(".rar");
    }

    /**
     * Extrai a possível extensão do arquivo da descrição
     */
    public String getExtensaoArquivo() {
        if (!pareceArquivo() || this.descricao == null) return "";
        
        String desc = this.descricao.toLowerCase();
        if (desc.contains(".pdf")) return "pdf";
        if (desc.contains(".doc")) return "doc";
        if (desc.contains(".xlsx")) return "xlsx";
        if (desc.contains(".png")) return "png";
        if (desc.contains(".jpg") || desc.contains(".jpeg")) return "jpg";
        if (desc.contains(".zip")) return "zip";
        if (desc.contains(".rar")) return "rar";
        
        return "";
    }
}
