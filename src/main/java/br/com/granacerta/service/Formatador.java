package br.com.granacerta.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Usado nas views como ${@fmt.moeda(valor)}. */
@Component("fmt")
public class Formatador {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String moedaStatic(BigDecimal v) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(v == null ? BigDecimal.ZERO : v).replace(' ', ' ');
    }

    public String moeda(BigDecimal v) {
        return moedaStatic(v);
    }

    public String data(LocalDate d) {
        return d == null ? "" : d.format(DATA);
    }

    public String dataHora(LocalDateTime d) {
        return d == null ? "" : d.format(DATA_HORA);
    }

    public String mesAno(java.time.YearMonth m) {
        if (m == null) return "";
        String s = m.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", PT_BR));
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public String numero(BigDecimal v) {
        NumberFormat nf = NumberFormat.getNumberInstance(PT_BR);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(v == null ? BigDecimal.ZERO : v);
    }
}
