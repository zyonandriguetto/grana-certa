package br.com.granacerta.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/** Formata valores nos textos gerados pelo servidor (ex.: dicas do painel). */
public final class Formatador {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private Formatador() {}

    public static String moedaStatic(BigDecimal v) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(v == null ? BigDecimal.ZERO : v).replace(' ', ' ');
    }
}
