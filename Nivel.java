package br.com.granacerta.model;

public enum Nivel {
    INICIANTE("Iniciante"),
    INTERMEDIARIO("Intermediário"),
    AVANCADO("Avançado");

    private final String rotulo;

    Nivel(String rotulo) { this.rotulo = rotulo; }

    public String getRotulo() { return rotulo; }
}
