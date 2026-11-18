package br.com.granacerta.controller;

import br.com.granacerta.service.SimuladorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/simuladores")
public class SimuladorController {

    private final SimuladorService service;

    public SimuladorController(SimuladorService service) {
        this.service = service;
    }

    @GetMapping
    public String inicio() {
        return "simuladores/index";
    }

    @GetMapping("/juros-compostos")
    public String juros(@RequestParam(defaultValue = "100") BigDecimal inicial,
                        @RequestParam(defaultValue = "50") BigDecimal mensal,
                        @RequestParam(defaultValue = "0.8") BigDecimal taxa,
                        @RequestParam(defaultValue = "60") int meses,
                        Model model) {
        meses = Math.max(1, Math.min(meses, 600));
        model.addAttribute("inicial", inicial.max(BigDecimal.ZERO));
        model.addAttribute("mensal", mensal.max(BigDecimal.ZERO));
        model.addAttribute("taxa", taxa.max(BigDecimal.ZERO));
        model.addAttribute("meses", meses);
        model.addAttribute("r", service.jurosCompostos(inicial.max(BigDecimal.ZERO), mensal.max(BigDecimal.ZERO),
                taxa.max(BigDecimal.ZERO), meses));
        return "simuladores/juros";
    }

    @GetMapping("/cartao-rotativo")
    public String rotativo(@RequestParam(defaultValue = "500") BigDecimal divida,
                           @RequestParam(defaultValue = "14") BigDecimal taxa,
                           @RequestParam(defaultValue = "12") int meses,
                           Model model) {
        meses = Math.max(1, Math.min(meses, 60));
        BigDecimal d = divida.max(BigDecimal.ONE);
        BigDecimal tx = taxa.max(BigDecimal.ZERO);
        model.addAttribute("divida", d);
        model.addAttribute("taxa", tx);
        model.addAttribute("meses", meses);
        model.addAttribute("r", service.rotativo(d, tx, meses));
        return "simuladores/rotativo";
    }

    @GetMapping("/parcelado-ou-avista")
    public String parcelamento(@RequestParam(defaultValue = "1200") BigDecimal preco,
                               @RequestParam(defaultValue = "10") BigDecimal desconto,
                               @RequestParam(defaultValue = "10") int parcelas,
                               @RequestParam(defaultValue = "0.8") BigDecimal rendimento,
                               Model model) {
        parcelas = Math.max(1, Math.min(parcelas, 48));
        BigDecimal p = preco.max(BigDecimal.ONE);
        BigDecimal desc = desconto.max(BigDecimal.ZERO).min(BigDecimal.valueOf(90));
        BigDecimal rend = rendimento.max(BigDecimal.ZERO);
        model.addAttribute("preco", p);
        model.addAttribute("desconto", desc);
        model.addAttribute("parcelas", parcelas);
        model.addAttribute("rendimento", rend);
        model.addAttribute("r", service.parcelamento(p, desc, parcelas, rend));
        return "simuladores/parcelamento";
    }
}
