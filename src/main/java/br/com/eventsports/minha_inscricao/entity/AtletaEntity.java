package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.Genero;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atletas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados do atleta/participante")
public class AtletaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único do atleta", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "Usuário é obrigatório")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @Schema(description = "Usuário associado ao atleta")
    private UsuarioEntity usuario;

    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", 
             message = "CPF deve estar no formato XXX.XXX.XXX-XX")
    @Column(name = "cpf", length = 14, unique = true)
    @Schema(description = "CPF do atleta", example = "123.456.789-00")
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    @Column(name = "data_nascimento", nullable = false)
    @Schema(description = "Data de nascimento do atleta", example = "1990-05-15", required = true)
    private LocalDate dataNascimento;

    @NotNull(message = "Gênero é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 20)
    @Schema(description = "Gênero do atleta", example = "MASCULINO", required = true)
    private Genero genero;

    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", 
             message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    @Column(name = "telefone", length = 15)
    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String telefone;

    @Size(max = 100, message = "Nome de emergência deve ter no máximo 100 caracteres")
    @Column(name = "emergencia_nome", length = 100)
    @Schema(description = "Nome do contato de emergência", example = "Maria Silva Santos")
    private String emergenciaNome;

    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", 
             message = "Telefone de emergência deve estar no formato (XX) XXXXX-XXXX")
    @Column(name = "emergencia_telefone", length = 15)
    @Schema(description = "Telefone do contato de emergência", example = "(11) 88888-8888")
    private String emergenciaTelefone;

    @Size(max = 50, message = "Parentesco deve ter no máximo 50 caracteres")
    @Column(name = "emergencia_parentesco", length = 50)
    @Schema(description = "Parentesco do contato de emergência", example = "Mãe")
    private String emergenciaParentesco;

    @Size(max = 500, message = "Observações médicas devem ter no máximo 500 caracteres")
    @Column(name = "observacoes_medicas", length = 500)
    @Schema(description = "Observações médicas importantes", 
            example = "Alérgico a anti-inflamatórios. Problema no joelho direito.")
    private String observacoesMedicas;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    @Column(name = "endereco", length = 200)
    @Schema(description = "Endereço completo", example = "Rua das Palmeiras, 456 - Vila Nova - São Paulo/SP")
    private String endereco;

    @Size(max = 100, message = "Equipe deve ter no máximo 100 caracteres")
    @Column(name = "equipe", length = 100)
    @Schema(description = "Equipe/Academia do atleta", example = "CrossFit Warriors")
    private String equipe;

    @Column(name = "aceita_termos", nullable = false)
    @Builder.Default
    @Schema(description = "Indica se aceitou os termos", example = "true", required = true)
    private Boolean aceitaTermos = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Data de criação", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Relacionamentos
    @OneToMany(mappedBy = "atleta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "Lista de inscrições do atleta", accessMode = Schema.AccessMode.READ_ONLY)
    private List<InscricaoEntity> inscricoes = new ArrayList<>();

    @ManyToMany(mappedBy = "atletas", fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "Lista de equipes das quais o atleta faz parte", accessMode = Schema.AccessMode.READ_ONLY)
    private List<EquipeEntity> equipes = new ArrayList<>();

    @OneToMany(mappedBy = "capitao", fetch = FetchType.LAZY)
    @Builder.Default
    @Schema(description = "Lista de equipes que o atleta capitaneia", accessMode = Schema.AccessMode.READ_ONLY)
    private List<EquipeEntity> equipesCapitaneadas = new ArrayList<>();

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.aceitaTermos == null) {
            this.aceitaTermos = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public int getIdade() {
        return this.dataNascimento != null 
               ? Period.between(this.dataNascimento, LocalDate.now()).getYears()
               : 0;
    }

    public boolean isMaiorIdade() {
        return getIdade() >= 18;
    }

    public boolean podeParticipar() {
        return this.usuario != null && 
               this.usuario.getAtivo() && 
               this.aceitaTermos;
    }

    public String getNomeCompleto() {
        return this.usuario != null ? this.usuario.getNome() : "";
    }

    public String getContatoEmergencia() {
        if (this.emergenciaNome == null || this.emergenciaTelefone == null) {
            return "Não informado";
        }
        String parentesco = this.emergenciaParentesco != null 
                           ? " (" + this.emergenciaParentesco + ")" 
                           : "";
        return this.emergenciaNome + parentesco + " - " + this.emergenciaTelefone;
    }

    public boolean temContatoEmergencia() {
        return this.emergenciaNome != null && 
               !this.emergenciaNome.trim().isEmpty() &&
               this.emergenciaTelefone != null && 
               !this.emergenciaTelefone.trim().isEmpty();
    }

    public int getTotalInscricoes() {
        return this.inscricoes != null ? this.inscricoes.size() : 0;
    }

    public long getInscricoesAtivas() {
        return this.inscricoes != null 
               ? this.inscricoes.stream()
                   .filter(inscricao -> inscricao.getStatus().isAtiva())
                   .count()
               : 0;
    }

    public int getTotalEquipes() {
        return this.equipes != null ? this.equipes.size() : 0;
    }

    public long getEquipesAtivas() {
        return this.equipes != null 
               ? this.equipes.stream()
                   .filter(EquipeEntity::getAtiva)
                   .count()
               : 0;
    }

    public int getTotalEquipesCapitaneadas() {
        return this.equipesCapitaneadas != null ? this.equipesCapitaneadas.size() : 0;
    }

    public long getEquipesCapitaneadasAtivas() {
        return this.equipesCapitaneadas != null 
               ? this.equipesCapitaneadas.stream()
                   .filter(EquipeEntity::getAtiva)
                   .count()
               : 0;
    }

    public boolean pertenceEquipe(EquipeEntity equipe) {
        return this.equipes != null && this.equipes.contains(equipe);
    }

    public boolean isCapitaoDeEquipe(EquipeEntity equipe) {
        return this.equipesCapitaneadas != null && this.equipesCapitaneadas.contains(equipe);
    }

    public boolean temEquipesAtivas() {
        return getEquipesAtivas() > 0;
    }

    public boolean isCapitao() {
        return getTotalEquipesCapitaneadas() > 0;
    }

    public List<EquipeEntity> getEquipesAtivasLista() {
        return this.equipes != null 
               ? this.equipes.stream()
                   .filter(EquipeEntity::getAtiva)
                   .toList()
               : new ArrayList<>();
    }

    public boolean podeSerCapitao() {
        return podeParticipar() && isMaiorIdade();
    }
}