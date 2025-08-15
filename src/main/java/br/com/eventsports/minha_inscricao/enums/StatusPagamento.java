package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define os status possíveis de um pagamento
 */
public enum StatusPagamento {
    
    PENDENTE("Pendente", "Pagamento aguardando processamento"),
    PROCESSANDO("Processando", "Pagamento sendo processado"),
    APROVADO("Aprovado", "Pagamento aprovado com sucesso"),
    RECUSADO("Recusado", "Pagamento recusado"),
    CANCELADO("Cancelado", "Pagamento cancelado pelo usuário"),
    ESTORNADO("Estornado", "Pagamento estornado"),
    EXPIRADO("Expirado", "Pagamento expirou por falta de ação");

    private final String nome;
    private final String descricao;

    StatusPagamento(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isFinalizado() {
        return this == APROVADO || this == RECUSADO || 
               this == CANCELADO || this == ESTORNADO || 
               this == EXPIRADO;
    }

    public boolean isAprovado() {
        return this == APROVADO;
    }

    public boolean podeSerCancelado() {
        return this == PENDENTE || this == PROCESSANDO;
    }

    public boolean podeSerEstornado() {
        return this == APROVADO;
    }

    @Override
    public String toString() {
        return nome;
    }
}
