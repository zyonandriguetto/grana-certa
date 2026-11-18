package br.com.granacerta.controller;

import br.com.granacerta.model.Categoria;
import br.com.granacerta.model.Classificacao;
import br.com.granacerta.model.TipoMovimento;
import br.com.granacerta.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @ModelAttribute
    public void opcoes(Model model) {
        model.addAttribute("tipos", TipoMovimento.values());
        model.addAttribute("classificacoes", Classificacao.values());
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categorias/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", service.buscar(id));
        return "categorias/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("categoria") Categoria categoria, BindingResult br, RedirectAttributes ra) {
        if (categoria.getTipo() == TipoMovimento.RECEITA && categoria.getClassificacao() != null
                && categoria.getClassificacao() != Classificacao.ENTRADA) {
            br.rejectValue("classificacao", "invalida", "Receitas usam a classificação \"Entrada de dinheiro\"");
        }
        if (categoria.getTipo() == TipoMovimento.DESPESA && categoria.getClassificacao() == Classificacao.ENTRADA) {
            br.rejectValue("classificacao", "invalida", "Despesas devem ser Necessidade, Desejo ou Poupança");
        }
        if (br.hasErrors()) return "categorias/form";
        service.salvar(categoria);
        ra.addFlashAttribute("sucesso", "Categoria \"" + categoria.getNome() + "\" salva.");
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Categoria excluída.");
        return "redirect:/categorias";
    }
}
