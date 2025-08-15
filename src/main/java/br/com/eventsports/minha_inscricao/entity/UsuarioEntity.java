package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"senha"})
@Schema(description = "Dados do usuário do sistema")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do usuário", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    @Column(name = "email", unique = true, nullable = false, length = 150)
    @Schema(description = "Email do usuário (login)", example = "joao@exemplo.com", required = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 100, message = "Senha deve ter entre 6 e 100 caracteres")
    @Column(name = "senha", nullable = false, length = 100)
    @Schema(description = "Senha do usuário (criptografada)", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String senha;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    @Schema(description = "Nome completo do usuário", example = "João Silva Santos", required = true)
    private String nome;

    @NotNull(message = "Tipo de usuário é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    @Schema(description = "Tipo do usuário no sistema", example = "ATLETA", required = true)
    private TipoUsuario tipo;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    @Schema(description = "Indica se o usuário está ativo", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean ativo = true;

    @Column(name = "ultimo_login")
    @Schema(description = "Data/hora do último login", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime ultimoLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Data de criação do usuário", accessMode = Schema.AccessMode.READ_ONLY)
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
    public boolean isOrganizador() {
        return TipoUsuario.ORGANIZADOR.equals(this.tipo);
    }

    public boolean isAtleta() {
        return TipoUsuario.ATLETA.equals(this.tipo);
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void registrarLogin() {
        this.ultimoLogin = LocalDateTime.now();
    }
}
