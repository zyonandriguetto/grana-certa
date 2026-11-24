package br.com.granacerta.model;

/** Classificação usada na regra 50-30-20 de orçamento pessoal. */
public enum Classificacao {
    NECESSIDADE("Necessidade", 50),
    DESEJO("Desejo", 30),
    POUPANCA("Poupança / Investimento", 20),
    ENTRADA("Entrada de dinheiro", 0);

    private final String rotulo;
    private final int percentualIdeal;

    Classificacao(String rotulo, int percentualIdeal) {
        this.rotulo = rotulo;
        this.percentualIdeal = percentualIdeal;
    }

    public String getRotulo() { return rotulo; }
    public int getPercentualIdeal() { return percentualIdeal; }
}
