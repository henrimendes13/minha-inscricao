package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define os tipos de usuário do sistema
 */
public enum TipoUsuario {

    ADMIN("ADMIN", "Administrador do sistema com acesso completo"),
    ORGANIZADOR("ORGANIZADOR", "Usuário responsável por criar e gerenciar eventos"),
    ATLETA("ATLETA", "Usuário que se inscreve em eventos esportivos");

    private final String nome;
    private final String descricao;

    TipoUsuario(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return nome;
    }
}
