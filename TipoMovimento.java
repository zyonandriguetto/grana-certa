package br.com.granacerta.model;

public enum TipoMovimento {
    RECEITA("Receita"),
    DESPESA("Despesa");

    private final String rotulo;

    TipoMovimento(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }
}
