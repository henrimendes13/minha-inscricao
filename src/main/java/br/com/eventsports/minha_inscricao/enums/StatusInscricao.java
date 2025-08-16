package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define os status possíveis de uma inscrição
 */
public enum StatusInscricao {
    
    PENDENTE("Pendente", "Inscrição realizada, aguardando pagamento"),
    CONFIRMADA("Confirmada", "Inscrição paga e confirmada"),
    CANCELADA("Cancelada", "Inscrição cancelada"),
    RECUSADA("Recusada", "Inscrição recusada"),
    EXPIRADA("Expirada", "Prazo de pagamento expirado"),
    LISTA_ESPERA("Lista de Espera", "Inscrição em lista de espera");

    private final String nome;
    private final String descricao;

    StatusInscricao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtiva() {
        return this == CONFIRMADA;
    }

    public boolean podeCancelar() {
        return this == PENDENTE || this == CONFIRMADA || this == LISTA_ESPERA;
    }

    public boolean podePagar() {
        return this == PENDENTE;
    }

    @Override
    public String toString() {
        return nome;
    }
}
