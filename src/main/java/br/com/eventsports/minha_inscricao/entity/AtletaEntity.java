package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.Genero;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "atletas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"evento", "inscricao", "equipe"})
public class AtletaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "cpf", length = 14, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", nullable = false, length = 20)
    private Genero genero;

    @Column(name = "telefone", length = 15)
    private String telefone;

    @Column(name = "emergencia_nome", length = 100)
    private String emergenciaNome;

    @Column(name = "emergencia_telefone", length = 15)
    private String emergenciaTelefone;

    @Column(name = "observacoes_medicas", length = 500)
    private String observacoesMedicas;

    @Column(name = "endereco", length = 200)
    private String endereco;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "aceita_termos", nullable = false)
    @Builder.Default
    private Boolean aceitaTermos = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamentos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id")
    private EventoEntity evento;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaEntity categoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscricao_id")
    private InscricaoEntity inscricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id")
    private EquipeEntity equipe;

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

    public boolean temContatoEmergencia() {
        return this.emergenciaNome != null && 
               !this.emergenciaNome.trim().isEmpty() &&
               this.emergenciaTelefone != null && 
               !this.emergenciaTelefone.trim().isEmpty();
    }

    public boolean temInscricao() {
        return this.inscricao != null;
    }

    public boolean inscricaoAtiva() {
        return this.inscricao != null && this.inscricao.isAtiva();
    }

    public boolean inscricaoConfirmada() {
        return this.inscricao != null && this.inscricao.isConfirmada();
    }

    public boolean inscricaoPendente() {
        return this.inscricao != null && this.inscricao.isPendente();
    }

    public boolean inscricaoCancelada() {
        return this.inscricao != null && this.inscricao.isCancelada();
    }

    public String getStatusInscricao() {
        return this.inscricao != null ? this.inscricao.getDescricaoStatus() : "Sem inscrição";
    }

    public String getNomeEventoInscricao() {
        return this.inscricao != null ? this.inscricao.getNomeEvento() : "";
    }

    public String getNomeCategoriaInscricao() {
        return this.inscricao != null ? this.inscricao.getNomeCategoria() : "";
    }

    public Long getInscricaoId() {
        return this.inscricao != null ? this.inscricao.getId() : null;
    }

    public boolean temEquipe() {
        return this.equipe != null;
    }

    public boolean equipeAtiva() {
        return this.equipe != null && this.equipe.getAtiva();
    }

    public boolean pertenceEquipe(EquipeEntity equipe) {
        return this.equipe != null && this.equipe.equals(equipe);
    }

    public boolean pertenceEquipe(Long equipeId) {
        return this.equipe != null && this.equipe.getId().equals(equipeId);
    }

    public String getNomeEquipe() {
        return this.equipe != null ? this.equipe.getNome() : "";
    }

    public Long getEquipeId() {
        return this.equipe != null ? this.equipe.getId() : null;
    }

    public boolean podeParticiparEquipe() {
        return this.aceitaTermos && isMaiorIdade();
    }

    // Métodos essenciais mantidos para compatibilidade
    public boolean podeParticipar() {
        return this.aceitaTermos;
    }

    public String getNomeCompleto() {
        return this.nome != null ? this.nome : "Atleta " + (this.id != null ? this.id : "Sem ID");
    }

    // Métodos para gerenciar evento
    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    public boolean temEvento() {
        return this.evento != null;
    }

    public boolean estaVinculadoAoEvento(EventoEntity evento) {
        return this.evento != null && this.evento.equals(evento);
    }

    public boolean estaVinculadoAoEvento(Long eventoId) {
        return this.evento != null && this.evento.getId().equals(eventoId);
    }

    // Métodos para gerenciar categoria
    public String getNomeCategoria() {
        return this.categoria != null ? this.categoria.getNome() : "";
    }

    public Long getCategoriaId() {
        return this.categoria != null ? this.categoria.getId() : null;
    }

    public boolean temCategoria() {
        return this.categoria != null;
    }

    public boolean estaVinculadoACategoria(CategoriaEntity categoria) {
        return this.categoria != null && this.categoria.equals(categoria);
    }

    public boolean estaVinculadoACategoria(Long categoriaId) {
        return this.categoria != null && this.categoria.getId().equals(categoriaId);
    }

    public boolean categoriaAtiva() {
        return this.categoria != null && this.categoria.getAtiva();
    }
}