package br.com.eventsports.minha_inscricao.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Dados do organizador de eventos")
public class OrganizadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do organizador", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Usuário é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @Schema(description = "Usuário associado ao organizador")
    private UsuarioEntity usuario;

    @Size(max = 100, message = "Nome da empresa deve ter no máximo 100 caracteres")
    @Column(name = "nome_empresa", length = 100)
    @Schema(description = "Nome da empresa organizadora", example = "EventSports LTDA")
    private String nomeEmpresa;

    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", 
             message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX")
    @Column(name = "cnpj", length = 18, unique = true)
    @Schema(description = "CNPJ da empresa", example = "12.345.678/0001-90")
    private String cnpj;

    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", 
             message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    @Column(name = "telefone", length = 15)
    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String telefone;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    @Column(name = "endereco", length = 200)
    @Schema(description = "Endereço completo", example = "Rua das Flores, 123 - Centro - São Paulo/SP")
    private String endereco;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Column(name = "descricao", length = 500)
    @Schema(description = "Descrição do organizador/empresa", 
            example = "Empresa especializada em eventos esportivos de CrossFit")
    private String descricao;

    @Size(max = 100, message = "Site deve ter no máximo 100 caracteres")
    @Column(name = "site", length = 100)
    @Schema(description = "Site da empresa", example = "https://www.eventsports.com.br")
    private String site;

    @Builder.Default
    @Column(name = "verificado", nullable = false)
    @Schema(description = "Indica se o organizador foi verificado", 
            example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean verificado = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Data de criação", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Relacionamentos
    @OneToMany(mappedBy = "organizador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "Lista de eventos criados pelo organizador", accessMode = Schema.AccessMode.READ_ONLY)
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