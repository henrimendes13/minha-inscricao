package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define as formas de pagamento disponíveis
 */
public enum FormaPagamento {
    
    PIX("PIX", "Pagamento instantâneo via PIX", true),
    CARTAO_CREDITO("Cartão de Crédito", "Pagamento via cartão de crédito", true),
    CARTAO_DEBITO("Cartão de Débito", "Pagamento via cartão de débito", true),
    BOLETO("Boleto Bancário", "Pagamento via boleto bancário", false),
    TRANSFERENCIA("Transferência Bancária", "Transferência bancária tradicional", false),
    DINHEIRO("Dinheiro", "Pagamento em dinheiro (presencial)", true);

    private final String nome;
    private final String descricao;
    private final boolean instantaneo;

    FormaPagamento(String nome, String descricao, boolean instantaneo) {
        this.nome = nome;
        this.descricao = descricao;
        this.instantaneo = instantaneo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isInstantaneo() {
        return instantaneo;
    }

    public boolean requerAprovacao() {
        return !instantaneo;
    }

    public static FormaPagamento[] getFormasInstantaneas() {
        return new FormaPagamento[]{PIX, CARTAO_CREDITO, CARTAO_DEBITO, DINHEIRO};
    }

    public static FormaPagamento[] getFormasNaoInstantaneas() {
        return new FormaPagamento[]{BOLETO, TRANSFERENCIA};
    }

    @Override
    public String toString() {
        return nome;
    }
}
