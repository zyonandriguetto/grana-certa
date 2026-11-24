package br.com.granacerta.controller;

import br.com.granacerta.service.SimuladorService;
import br.com.granacerta.service.SimuladorService.ResultadoInvestimento;
import br.com.granacerta.service.SimuladorService.ResultadoParcelamento;
import br.com.granacerta.service.SimuladorService.ResultadoRotativo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/** Simuladores educativos: não gravam nada no banco. Valores fora dos limites são ajustados. */
@RestController
@RequestMapping("/api/simuladores")
public class SimuladorController {

    public record Simulador(String nome, String caminho, String descricao) {}

    public record JurosCompostos(BigDecimal inicial, BigDecimal mensal, BigDecimal taxa, int meses,
                                 ResultadoInvestimento resultado) {}

    public record CartaoRotativo(BigDecimal divida, BigDecimal taxa, int meses, ResultadoRotativo resultado) {}

    public record ParceladoOuAvista(BigDecimal preco, BigDecimal desconto, int parcelas, BigDecimal rendimento,
                                    ResultadoParcelamento resultado) {}

    private final SimuladorService service;

    public SimuladorController(SimuladorService service) {
        this.service = service;
    }

    @GetMapping
    public List<Simulador> listar() {
        return List.of(
                new Simulador("Juros compostos", "/api/simuladores/juros-compostos",
                        "Quanto vira o seu dinheiro guardando um pouco todo mês?"),
                new Simulador("Rotativo do cartão", "/api/simuladores/cartao-rotativo",
                        "O que acontece com uma fatura que não foi paga, com e sem o teto da Lei 14.690/2023."),
                new Simulador("À vista ou parcelado?", "/api/simuladores/parcelado-ou-avista",
                        "Calcule os juros escondidos quando a loja dá desconto à vista."));
    }

    @GetMapping("/juros-compostos")
    public JurosCompostos juros(@RequestParam(defaultValue = "100") BigDecimal inicial,
                                @RequestParam(defaultValue = "50") BigDecimal mensal,
                                @RequestParam(defaultValue = "0.8") BigDecimal taxa,
                                @RequestParam(defaultValue = "60") int meses) {
        int m = Math.max(1, Math.min(meses, 600));
        BigDecimal ini = inicial.max(BigDecimal.ZERO);
        BigDecimal mens = mensal.max(BigDecimal.ZERO);
        BigDecimal tx = taxa.max(BigDecimal.ZERO);
        return new JurosCompostos(ini, mens, tx, m, service.jurosCompostos(ini, mens, tx, m));
    }

    @GetMapping("/cartao-rotativo")
    public CartaoRotativo rotativo(@RequestParam(defaultValue = "500") BigDecimal divida,
                                   @RequestParam(defaultValue = "14") BigDecimal taxa,
                                   @RequestParam(defaultValue = "12") int meses) {
        int m = Math.max(1, Math.min(meses, 60));
        BigDecimal d = divida.max(BigDecimal.ONE);
        BigDecimal tx = taxa.max(BigDecimal.ZERO);
        return new CartaoRotativo(d, tx, m, service.rotativo(d, tx, m));
    }

    @GetMapping("/parcelado-ou-avista")
    public ParceladoOuAvista parcelamento(@RequestParam(defaultValue = "1200") BigDecimal preco,
                                          @RequestParam(defaultValue = "10") BigDecimal desconto,
                                          @RequestParam(defaultValue = "10") int parcelas,
                                          @RequestParam(defaultValue = "0.8") BigDecimal rendimento) {
        int n = Math.max(1, Math.min(parcelas, 48));
        BigDecimal p = preco.max(BigDecimal.ONE);
        BigDecimal desc = desconto.max(BigDecimal.ZERO).min(BigDecimal.valueOf(90));
        BigDecimal rend = rendimento.max(BigDecimal.ZERO);
        return new ParceladoOuAvista(p, desc, n, rend, service.parcelamento(p, desc, n, rend));
    }
}
