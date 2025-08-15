package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.Genero;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Categoria de competição dentro de um evento")
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da categoria", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Evento é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    @Schema(description = "Evento ao qual a categoria pertence")
    private EventoEntity evento;

    @NotBlank(message = "Nome da categoria é obrigatório")
    @Size(max = 100, message = "Nome da categoria deve ter no máximo 100 caracteres")
    @Column(name = "nome", nullable = false, length = 100)
    @Schema(description = "Nome da categoria", example = "Masculino Elite", required = true)
    private String nome;

    @Size(max = 300, message = "Descrição deve ter no máximo 300 caracteres")
    @Column(name = "descricao", length = 300)
    @Schema(description = "Descrição detalhada da categoria", 
            example = "Categoria para atletas masculinos experientes, acima de 25 anos")
    private String descricao;

    @Min(value = 16, message = "Idade mínima deve ser pelo menos 16 anos")
    @Max(value = 80, message = "Idade mínima não pode ser maior que 80 anos")
    @Column(name = "idade_minima")
    @Schema(description = "Idade mínima para participar", example = "18")
    private Integer idadeMinima;

    @Min(value = 16, message = "Idade máxima deve ser pelo menos 16 anos")
    @Max(value = 100, message = "Idade máxima não pode ser maior que 100 anos")
    @Column(name = "idade_maxima")
    @Schema(description = "Idade máxima para participar", example = "35")
    private Integer idadeMaxima;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 20)
    @Schema(description = "Gênero específico da categoria (null = misto)", example = "MASCULINO")
    private Genero genero;

    @NotNull(message = "Valor da inscrição é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Valor da inscrição deve ser maior ou igual a zero")
    @Digits(integer = 8, fraction = 2, message = "Valor deve ter no máximo 8 dígitos inteiros e 2 decimais")
    @Column(name = "valor_inscricao", nullable = false, precision = 10, scale = 2)
    @Schema(description = "Valor da inscrição para esta categoria", example = "150.00", required = true)
    private BigDecimal valorInscricao;

    @Min(value = 1, message = "Limite de participantes deve ser pelo menos 1")
    @Max(value = 10000, message = "Limite de participantes não pode ser maior que 10.000")
    @Column(name = "limite_participantes")
    @Schema(description = "Número máximo de participantes", example = "50")
    private Integer limiteParticipantes;

    @Builder.Default
    @Column(name = "ativa", nullable = false)
    @Schema(description = "Indica se a categoria está ativa para inscrições", example = "true")
    private Boolean ativa = true;

    @Size(max = 200, message = "Requisitos devem ter no máximo 200 caracteres")
    @Column(name = "requisitos", length = 200)
    @Schema(description = "Requisitos específicos para participar", 
            example = "Experiência mínima de 2 anos em CrossFit")
    private String requisitos;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Data de criação", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Relacionamentos
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "Lista de inscrições nesta categoria", accessMode = Schema.AccessMode.READ_ONLY)
    private List<InscricaoEntity> inscricoes = new ArrayList<>();

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
    public boolean atletaPodeParticipar(AtletaEntity atleta) {
        if (!this.ativa) {
            return false;
        }

        // Verificar gênero
        if (this.genero != null && !this.genero.equals(atleta.getGenero())) {
            return false;
        }

        int idade = atleta.getIdade();

        // Verificar idade mínima
        if (this.idadeMinima != null && idade < this.idadeMinima) {
            return false;
        }

        // Verificar idade máxima
        if (this.idadeMaxima != null && idade > this.idadeMaxima) {
            return false;
        }

        return true;
    }

    public boolean temVagasDisponiveis() {
        if (this.limiteParticipantes == null) {
            return true; // Sem limite
        }

        long inscricoesAtivas = getNumeroInscricoesAtivas();
        return inscricoesAtivas < this.limiteParticipantes;
    }

    public long getNumeroInscricoesAtivas() {
        return this.inscricoes != null 
               ? this.inscricoes.stream()
                   .filter(inscricao -> inscricao.getStatus().isAtiva())
                   .count()
               : 0;
    }

    public int getVagasRestantes() {
        if (this.limiteParticipantes == null) {
            return Integer.MAX_VALUE; // Ilimitado
        }

        return Math.max(0, this.limiteParticipantes - (int) getNumeroInscricoesAtivas());
    }

    public String getDescricaoCompleta() {
        StringBuilder sb = new StringBuilder(this.nome);
        
        if (this.genero != null) {
            sb.append(" - ").append(this.genero.getDescricao());
        }
        
        if (this.idadeMinima != null || this.idadeMaxima != null) {
            sb.append(" (");
            if (this.idadeMinima != null) {
                sb.append(this.idadeMinima).append("+");
            }
            if (this.idadeMaxima != null) {
                if (this.idadeMinima != null) {
                    sb.append(" até ");
                }
                sb.append(this.idadeMaxima).append(" anos");
            }
            sb.append(")");
        }
        
        return sb.toString();
    }

    public void ativar() {
        this.ativa = true;
    }

    public void desativar() {
        this.ativa = false;
    }

    public boolean isLotada() {
        return !temVagasDisponiveis();
    }
}
