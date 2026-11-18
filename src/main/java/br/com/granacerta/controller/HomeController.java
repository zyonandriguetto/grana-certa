package br.com.granacerta.controller;

import br.com.granacerta.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UsuarioService usuarios;
    private final CategoriaService categorias;
    private final TransacaoService transacoes;
    private final MetaService metas;
    private final LicaoService licoes;
    private final PerguntaService perguntas;
    private final TentativaService tentativas;

    public HomeController(UsuarioService usuarios, CategoriaService categorias, TransacaoService transacoes,
                          MetaService metas, LicaoService licoes, PerguntaService perguntas,
                          TentativaService tentativas) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.transacoes = transacoes;
        this.metas = metas;
        this.licoes = licoes;
        this.perguntas = perguntas;
        this.tentativas = tentativas;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("totalUsuarios", usuarios.contar());
        model.addAttribute("totalCategorias", categorias.contar());
        model.addAttribute("totalTransacoes", transacoes.contar());
        model.addAttribute("totalMetas", metas.contar());
        model.addAttribute("totalLicoes", licoes.contar());
        model.addAttribute("totalPerguntas", perguntas.contar());
        model.addAttribute("totalTentativas", tentativas.contar());
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("licoes", licoes.listar());
        return "index";
    }

    @GetMapping("/ranking")
    public String ranking(Model model) {
        model.addAttribute("ranking", usuarios.ranking());
        return "ranking";
    }

    @GetMapping("/sobre")
    public String sobre() {
        return "sobre";
    }
}
