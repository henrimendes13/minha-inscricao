package br.com.eventsports.minha_inscricao.enums;

/**
 * Enum que define os tipos de resultado possíveis para um workout
 */
public enum TipoWorkout {
    
    REPS("Repetições", "Resultado baseado no número de repetições executadas"),
    PESO("Peso", "Resultado baseado no peso levantado/carregado"),
    TEMPO("Tempo", "Resultado baseado no tempo de execução");

    private final String nome;
    private final String descricao;

    TipoWorkout(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Verifica se o tipo de workout é baseado em repetições
     */
    public boolean isReps() {
        return this == REPS;
    }

    /**
     * Verifica se o tipo de workout é baseado em peso
     */
    public boolean isPeso() {
        return this == PESO;
    }

    /**
     * Verifica se o tipo de workout é baseado em tempo
     */
    public boolean isTempo() {
        return this == TEMPO;
    }

    @Override
    public String toString() {
        return nome;
    }
}
