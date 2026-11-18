package br.com.granacerta.service;

import br.com.granacerta.model.*;
import br.com.granacerta.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/** Monta os indicadores do painel financeiro de um usuário. */
@Service
public class PainelService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final TransacaoRepository transacoes;
    private final UsuarioService usuarioService;

    public PainelService(TransacaoRepository transacoes, UsuarioService usuarioService) {
        this.transacoes = transacoes;
        this.usuarioService = usuarioService;
    }

    public record FatiaCategoria(String nome, String cor, String icone, BigDecimal valor, int percentual) {}

    public record Regra503020(Classificacao classificacao, BigDecimal valor, int percentualAtual, int percentualIdeal, boolean dentro) {}

    public record Insight(String tipo, String icone, String texto) {}

    public record Painel(Usuario usuario, YearMonth mes,
                         BigDecimal receitas, BigDecimal despesas, BigDecimal saldo,
                         BigDecimal impulsivas, int score, String scoreRotulo,
                         List<FatiaCategoria> porCategoria, List<Regra503020> regra,
                         List<String> mesesRotulos, List<BigDecimal> receitasSerie, List<BigDecimal> despesasSerie,
                         List<Insight> insights, List<Transacao> ultimas, List<Meta> metas) {}

    @Transactional(readOnly = true)
    public Painel montar(Long usuarioId, YearMonth mes) {
        Usuario u = usuarioService.buscar(usuarioId);
        List<Transacao> doMes = transacoes.doPeriodo(usuarioId, mes.atDay(1), mes.atEndOfMonth());

        BigDecimal receitas = soma(doMes.stream().filter(Transacao::isReceita).toList());
        List<Transacao> saidas = doMes.stream().filter(t -> !t.isReceita()).toList();
        BigDecimal despesas = soma(saidas);
        BigDecimal saldo = receitas.subtract(despesas);
        BigDecimal impulsivas = soma(saidas.stream().filter(Transacao::isImpulsiva).toList());

        // Gastos por categoria
        Map<Categoria, BigDecimal> porCat = new LinkedHashMap<>();
        saidas.forEach(t -> porCat.merge(t.getCategoria(), t.getValor(), BigDecimal::add));
        List<FatiaCategoria> fatias = porCat.entrySet().stream()
                .sorted(Map.Entry.<Categoria, BigDecimal>comparingByValue().reversed())
                .map(e -> new FatiaCategoria(e.getKey().getNome(), e.getKey().getCor(), e.getKey().getIcone(),
                        e.getValue(), pct(e.getValue(), despesas)))
                .toList();

        // Regra 50-30-20 sobre a renda do mês (receitas; se não houver, usa a renda cadastrada)
        BigDecimal base = receitas.signum() > 0 ? receitas : u.getRendaMensal();
        List<Regra503020> regra = new ArrayList<>();
        for (Classificacao c : List.of(Classificacao.NECESSIDADE, Classificacao.DESEJO, Classificacao.POUPANCA)) {
            BigDecimal v = soma(saidas.stream().filter(t -> t.getCategoria().getClassificacao() == c).toList());
            int atual = pct(v, base);
            boolean dentro = c == Classificacao.POUPANCA ? atual >= c.getPercentualIdeal() : atual <= c.getPercentualIdeal();
            regra.add(new Regra503020(c, v, atual, c.getPercentualIdeal(), dentro));
        }

        // Evolução dos últimos 6 meses
        List<String> rotulos = new ArrayList<>();
        List<BigDecimal> recSerie = new ArrayList<>();
        List<BigDecimal> despSerie = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth m = mes.minusMonths(i);
            List<Transacao> ts = transacoes.doPeriodo(usuarioId, m.atDay(1), m.atEndOfMonth());
            rotulos.add(m.getMonth().getDisplayName(TextStyle.SHORT, PT_BR) + "/" + String.valueOf(m.getYear()).substring(2));
            recSerie.add(soma(ts.stream().filter(Transacao::isReceita).toList()));
            despSerie.add(soma(ts.stream().filter(t -> !t.isReceita()).toList()));
        }

        int score = calcularScore(receitas, despesas, regra, impulsivas, u);
        List<Insight> insights = gerarInsights(u, receitas, despesas, saldo, impulsivas, fatias, regra);

        List<Transacao> ultimas = transacoes.findByUsuarioIdOrderByDataDescIdDesc(usuarioId).stream().limit(6).toList();
        List<Meta> metas = u.getMetas().stream().sorted(Comparator.comparing(Meta::getPrazo)).toList();

        return new Painel(u, mes, receitas, despesas, saldo, impulsivas, score, rotuloScore(score),
                fatias, regra, rotulos, recSerie, despSerie, insights, ultimas, metas);
    }

    /** Pontuação de 0 a 100 da saúde financeira do mês. */
    private int calcularScore(BigDecimal receitas, BigDecimal despesas, List<Regra503020> regra,
                              BigDecimal impulsivas, Usuario u) {
        if (receitas.signum() == 0 && despesas.signum() == 0) return 50;
        int score = 0;
        if (receitas.compareTo(despesas) >= 0) score += 30;            // não gastou mais do que ganhou
        for (Regra503020 r : regra) if (r.dentro()) score += 15;       // até 45 pela regra 50-30-20
        int impulsoPct = pct(impulsivas, despesas.signum() > 0 ? despesas : BigDecimal.ONE);
        if (impulsoPct <= 10) score += 15; else if (impulsoPct <= 25) score += 7;
        boolean temMetaAtiva = u.getMetas().stream().anyMatch(m -> !m.isConcluida());
        if (temMetaAtiva) score += 10;
        return Math.min(score, 100);
    }

    private String rotuloScore(int s) {
        if (s >= 80) return "Excelente";
        if (s >= 60) return "No caminho certo";
        if (s >= 40) return "Atenção";
        return "Hora de reorganizar";
    }

    private List<Insight> gerarInsights(Usuario u, BigDecimal receitas, BigDecimal despesas, BigDecimal saldo,
                                        BigDecimal impulsivas, List<FatiaCategoria> fatias, List<Regra503020> regra) {
        List<Insight> lista = new ArrayList<>();
        if (saldo.signum() < 0) {
            lista.add(new Insight("danger", "bi-exclamation-octagon",
                    "Você gastou mais do que recebeu neste mês. Evite cobrir a diferença com o rotativo do cartão ou cheque especial: são as linhas de crédito mais caras do mercado."));
        } else if (receitas.signum() > 0) {
            lista.add(new Insight("success", "bi-check-circle",
                    "Mês no azul! Sobraram " + moeda(saldo) + ". Que tal mandar uma parte para uma meta?"));
        }
        regra.stream().filter(r -> r.classificacao() == Classificacao.DESEJO && !r.dentro()).findFirst().ifPresent(r ->
                lista.add(new Insight("warning", "bi-bag",
                        "Os desejos consumiram " + r.percentualAtual() + "% da renda (ideal: até 30%). Tente a regra das 48h antes de comprar algo que não estava planejado.")));
        regra.stream().filter(r -> r.classificacao() == Classificacao.POUPANCA && !r.dentro()).findFirst().ifPresent(r ->
                lista.add(new Insight("info", "bi-piggy-bank",
                        "Você guardou " + r.percentualAtual() + "% da renda. A meta é 20%: comece pagando a si mesmo primeiro, logo que o dinheiro entrar.")));
        if (impulsivas.signum() > 0) {
            lista.add(new Insight("warning", "bi-lightning",
                    "Compras por impulso somaram " + moeda(impulsivas) + ". Esse valor investido a 0,8% ao mês viraria "
                            + moeda(SimuladorService.valorFuturoAportes(impulsivas, new BigDecimal("0.8"), 12)) + " em 1 ano se repetido todo mês."));
        }
        if (!fatias.isEmpty() && fatias.get(0).percentual() >= 35) {
            FatiaCategoria f = fatias.get(0);
            lista.add(new Insight("info", "bi-pie-chart",
                    "\"" + f.nome() + "\" representa " + f.percentual() + "% dos seus gastos. É a categoria com mais espaço para economizar."));
        }
        if (u.getMetas().isEmpty()) {
            lista.add(new Insight("info", "bi-flag",
                    "Você ainda não tem metas. Quem escreve um objetivo com valor e prazo tem muito mais chance de alcançá-lo."));
        }
        return lista;
    }

    private static BigDecimal soma(List<Transacao> ts) {
        return ts.stream().map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static int pct(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) return 0;
        return parte.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.HALF_UP).intValue();
    }

    private static String moeda(BigDecimal v) {
        return Formatador.moedaStatic(v);
    }
}
