package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define os gêneros para categorização de atletas
 */
public enum Genero {
    
    MASCULINO("Masculino", "M"),
    FEMININO("Feminino", "F"),
    OUTRO("Outro", "O"),
    NAO_INFORMADO("Não Informado", "N");

    private final String descricao;
    private final String sigla;

    Genero(String descricao, String sigla) {
        this.descricao = descricao;
        this.sigla = sigla;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSigla() {
        return sigla;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
