package br.com.eventsports.minha_inscricao.entity;

import br.com.eventsports.minha_inscricao.enums.Genero;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"senha", "eventosOrganizados", "inscricoes"})
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "senha", nullable = false, length = 100)
    private String senha;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    // Campos consolidados de AtletaEntity
    @Column(name = "cpf", length = 14, unique = true)
    private String cpf;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 20)
    private Genero genero;

    @Column(name = "telefone", length = 15)
    private String telefone;

    @Column(name = "endereco", length = 200)
    private String endereco;

    @Column(name = "emergencia_nome", length = 100)
    private String emergenciaNome;

    @Column(name = "emergencia_telefone", length = 15)
    private String emergenciaTelefone;

    @Column(name = "observacoes_medicas", length = 500)
    private String observacoesMedicas;

    @Column(name = "aceita_termos", nullable = false)
    @Builder.Default
    private Boolean aceitaTermos = false;

    // Campos consolidados de OrganizadorEntity
    @Column(name = "nome_empresa", length = 100)
    private String nomeEmpresa;

    @Column(name = "cnpj", length = 18, unique = true)
    private String cnpj;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "site", length = 100)
    private String site;

    @Builder.Default
    @Column(name = "verificado", nullable = false)
    private Boolean verificado = false;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamentos diretos
    @OneToMany(mappedBy = "organizador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventoEntity> eventosOrganizados = new ArrayList<>();

    @OneToMany(mappedBy = "atleta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InscricaoEntity> inscricoes = new ArrayList<>();

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.ativo == null) {
            this.ativo = true;
        }
        if (this.aceitaTermos == null) {
            this.aceitaTermos = false;
        }
        if (this.verificado == null) {
            this.verificado = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência para tipo de usuário
    public boolean isOrganizador() {
        return this.eventosOrganizados != null && !this.eventosOrganizados.isEmpty();
    }

    public boolean isAtleta() {
        return this.inscricoes != null && !this.inscricoes.isEmpty();
    }

    public TipoUsuario getTipoUsuario() {
        // Delega para TipoUsuarioUtil para lógica centralizada
        // ADMIN é reservado exclusivamente para desenvolvedores/sistema
        return br.com.eventsports.minha_inscricao.util.TipoUsuarioUtil.determinarTipo(this);
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

    // Métodos de organizador
    public void verificar() {
        this.verificado = true;
    }

    public void removerVerificacao() {
        this.verificado = false;
    }

    public boolean podeOrganizarEventos() {
        return this.ativo && this.verificado;
    }

    public String getNomeExibicao() {
        return this.nomeEmpresa != null && !this.nomeEmpresa.trim().isEmpty() 
               ? this.nomeEmpresa 
               : this.nome;
    }

    public int getTotalEventos() {
        return this.eventosOrganizados != null ? this.eventosOrganizados.size() : 0;
    }

    // Métodos de atleta
    public int getIdade() {
        return this.dataNascimento != null 
               ? java.time.Period.between(this.dataNascimento, LocalDate.now()).getYears()
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

    public boolean podeParticipar() {
        return this.aceitaTermos && this.ativo;
    }

    public String getNomeCompleto() {
        return this.nome != null ? this.nome : "Usuário " + (this.id != null ? this.id : "Sem ID");
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

    // Métodos para gerenciar relacionamentos
    public void adicionarEvento(EventoEntity evento) {
        if (this.eventosOrganizados == null) {
            this.eventosOrganizados = new ArrayList<>();
        }
        
        if (!this.eventosOrganizados.contains(evento)) {
            this.eventosOrganizados.add(evento);
            evento.setOrganizador(this);
        }
    }

    public void removerEvento(EventoEntity evento) {
        if (this.eventosOrganizados != null) {
            this.eventosOrganizados.remove(evento);
            if (evento.getOrganizador() != null && evento.getOrganizador().equals(this)) {
                evento.setOrganizador(null);
            }
        }
    }

    public void adicionarInscricao(InscricaoEntity inscricao) {
        if (this.inscricoes == null) {
            this.inscricoes = new ArrayList<>();
        }
        
        if (!this.inscricoes.contains(inscricao)) {
            this.inscricoes.add(inscricao);
            inscricao.setAtleta(this);
        }
    }

    public void removerInscricao(InscricaoEntity inscricao) {
        if (this.inscricoes != null) {
            this.inscricoes.remove(inscricao);
            if (inscricao.getAtleta() != null && inscricao.getAtleta().equals(this)) {
                inscricao.setAtleta(null);
            }
        }
    }
}
