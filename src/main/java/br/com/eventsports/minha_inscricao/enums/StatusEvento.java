package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define os status possíveis de um evento
 */
public enum StatusEvento {
    
    RASCUNHO("Rascunho", "Evento em criação, ainda não publicado"),
    ABERTO("Aberto para Inscrições", "Evento publicado e aceitando inscrições"),
    INSCRICOES_ENCERRADAS("Inscrições Encerradas", "Evento não aceita mais inscrições"),
    EM_ANDAMENTO("Em Andamento", "Evento acontecendo no momento"),
    FINALIZADO("Finalizado", "Evento concluído com sucesso"),
    CANCELADO("Cancelado", "Evento cancelado pelo organizador"),
    ADIADO("Adiado", "Evento adiado para nova data");

    private final String nome;
    private final String descricao;

    StatusEvento(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean podeReceberInscricoes() {
        return this == ABERTO;
    }

    public boolean podeSerEditado() {
        return this == RASCUNHO || this == ABERTO;
    }

    @Override
    public String toString() {
        return nome;
    }
}
