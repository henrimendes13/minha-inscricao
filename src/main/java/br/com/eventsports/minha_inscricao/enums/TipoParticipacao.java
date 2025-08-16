package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define os tipos de participação em um evento
 */
public enum TipoParticipacao {
    
    INDIVIDUAL("Individual", "Competição individual"),
    EQUIPE("Equipe", "Competição em equipe");

    private final String nome;
    private final String descricao;

    TipoParticipacao(String nome, String descricao) {
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
