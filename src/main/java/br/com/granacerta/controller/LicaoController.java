package br.com.granacerta.controller;

import br.com.granacerta.model.Licao;
import br.com.granacerta.model.Nivel;
import br.com.granacerta.service.LicaoService;
import br.com.granacerta.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/licoes")
public class LicaoController {

    private final LicaoService service;
    private final UsuarioService usuarios;

    public LicaoController(LicaoService service, UsuarioService usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("licoes", service.listar());
        return "licoes/lista";
    }

    /** Página de leitura da lição, com botão para fazer o quiz. */
    @GetMapping("/{id}")
    public String ler(@PathVariable Long id, Model model) {
        model.addAttribute("licao", service.buscar(id));
        model.addAttribute("usuarios", usuarios.listar());
        return "licoes/detalhe";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Licao l = new Licao();
        l.setOrdem((int) service.contar() + 1);
        model.addAttribute("licao", l);
        model.addAttribute("niveis", Nivel.values());
        return "licoes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("licao", service.buscar(id));
        model.addAttribute("niveis", Nivel.values());
        return "licoes/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("licao") Licao licao, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("niveis", Nivel.values());
            return "licoes/form";
        }
        Licao salva = service.salvar(licao);
        ra.addFlashAttribute("sucesso", "Lição \"" + salva.getTitulo() + "\" salva.");
        return "redirect:/licoes/" + salva.getId();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Lição excluída junto com suas perguntas e tentativas.");
        return "redirect:/licoes";
    }
}
