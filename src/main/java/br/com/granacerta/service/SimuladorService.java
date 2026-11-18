package br.com.granacerta.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Simuladores educativos. Não salvam nada no banco: servem para mostrar,
 * com números, o efeito dos juros a favor e contra o jovem.
 */
@Service
public class SimuladorService {

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    // ---------- 1. Juros compostos (dinheiro trabalhando a seu favor) ----------

    public record PontoSerie(int mes, BigDecimal investido, BigDecimal saldo) {}

    public record ResultadoInvestimento(BigDecimal totalInvestido, BigDecimal saldoFinal, BigDecimal juros,
                                        List<PontoSerie> serie) {}

    public ResultadoInvestimento jurosCompostos(BigDecimal inicial, BigDecimal mensal, BigDecimal taxaMensalPct, int meses) {
        BigDecimal i = taxaMensalPct.divide(CEM, MC);
        BigDecimal saldo = inicial;
        BigDecimal investido = inicial;
        List<PontoSerie> serie = new ArrayList<>();
        serie.add(new PontoSerie(0, r(investido), r(saldo)));
        for (int m = 1; m <= meses; m++) {
            saldo = saldo.multiply(BigDecimal.ONE.add(i), MC).add(mensal);
            investido = investido.add(mensal);
            serie.add(new PontoSerie(m, r(investido), r(saldo)));
        }
        return new ResultadoInvestimento(r(investido), r(saldo), r(saldo.subtract(investido)), serie);
    }

    /** Valor futuro de aportes mensais iguais (usado nos alertas do painel). */
    public static BigDecimal valorFuturoAportes(BigDecimal aporte, BigDecimal taxaMensalPct, int meses) {
        BigDecimal i = taxaMensalPct.divide(CEM, MC);
        BigDecimal saldo = BigDecimal.ZERO;
        for (int m = 0; m < meses; m++) saldo = saldo.multiply(BigDecimal.ONE.add(i), MC).add(aporte);
        return r(saldo);
    }

    // ---------- 2. Rotativo do cartão (dinheiro trabalhando contra você) ----------

    public record PontoDivida(int mes, BigDecimal semTeto, BigDecimal comTeto) {}

    public record ResultadoRotativo(BigDecimal divida, BigDecimal semTeto, BigDecimal comTeto,
                                    BigDecimal taxaAnualEquivalentePct, List<PontoDivida> serie) {}

    /**
     * Simula a dívida do cartão crescendo no rotativo sem nenhum pagamento.
     * "Com teto" aplica a Lei 14.690/2023: desde jan/2024, juros e encargos do
     * rotativo e do parcelamento da fatura não podem passar de 100% da dívida original.
     */
    public ResultadoRotativo rotativo(BigDecimal divida, BigDecimal taxaMensalPct, int meses) {
        BigDecimal i = taxaMensalPct.divide(CEM, MC);
        BigDecimal teto = divida.multiply(BigDecimal.TWO);
        BigDecimal saldo = divida;
        List<PontoDivida> serie = new ArrayList<>();
        serie.add(new PontoDivida(0, r(divida), r(divida)));
        for (int m = 1; m <= meses; m++) {
            saldo = saldo.multiply(BigDecimal.ONE.add(i), MC);
            serie.add(new PontoDivida(m, r(saldo), r(saldo.min(teto))));
        }
        BigDecimal anual = BigDecimal.ONE.add(i).pow(12, MC).subtract(BigDecimal.ONE).multiply(CEM);
        return new ResultadoRotativo(r(divida), r(saldo), r(saldo.min(teto)), r(anual), serie);
    }

    // ---------- 3. À vista com desconto x parcelado ----------

    public record ResultadoParcelamento(BigDecimal precoVista, BigDecimal totalParcelado, BigDecimal valorParcela,
                                        BigDecimal diferenca,
                                        BigDecimal taxaImplicitaMensalPct, BigDecimal rendimentoSeGuardasse,
                                        boolean melhorAVista) {}

    /**
     * Descobre a taxa de juros "escondida" num parcelamento sem juros quando
     * existe desconto para pagamento à vista, e compara com o rendimento de
     * deixar o dinheiro aplicado.
     */
    public ResultadoParcelamento parcelamento(BigDecimal precoCheio, BigDecimal descontoVistaPct, int parcelas,
                                              BigDecimal rendimentoMensalPct) {
        BigDecimal precoVista = precoCheio.multiply(BigDecimal.ONE.subtract(descontoVistaPct.divide(CEM, MC)), MC);
        BigDecimal parcela = precoCheio.divide(BigDecimal.valueOf(parcelas), MC);
        // Parcelas iguais, a primeira paga em 30 dias (série postecipada)
        double taxa = taxaImplicita(precoVista.doubleValue(), parcela.doubleValue(), parcelas);

        // Se guardasse o valor à vista e fosse pagando as parcelas, quanto renderia?
        BigDecimal i = rendimentoMensalPct.divide(CEM, MC);
        BigDecimal saldo = precoVista;
        for (int m = 1; m <= parcelas; m++) {
            saldo = saldo.multiply(BigDecimal.ONE.add(i), MC).subtract(parcela);
        }
        BigDecimal taxaPct = BigDecimal.valueOf(taxa * 100);
        return new ResultadoParcelamento(r(precoVista), r(precoCheio), r(parcela), r(precoCheio.subtract(precoVista)),
                r(taxaPct), r(saldo), taxaPct.compareTo(rendimentoMensalPct) > 0);
    }

    /** Taxa i que faz VP = parcela * (1 - (1+i)^-n) / i, por bisseção. */
    static double taxaImplicita(double vp, double parcela, int n) {
        if (parcela * n <= vp + 1e-9) return 0;
        double lo = 0, hi = 1;
        for (int k = 0; k < 200; k++) {
            double mid = (lo + hi) / 2;
            double v = parcela * (1 - Math.pow(1 + mid, -n)) / mid;
            if (v > vp) lo = mid; else hi = mid;
        }
        return (lo + hi) / 2;
    }

    private static BigDecimal r(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }
}
