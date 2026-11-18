package br.com.granacerta.controller;

import br.com.granacerta.model.Usuario;
import br.com.granacerta.service.PainelService;
import br.com.granacerta.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final PainelService painelService;

    public UsuarioController(UsuarioService service, PainelService painelService) {
        this.service = service;
        this.painelService = painelService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", service.listar());
        return "usuarios/lista";
    }

    /** Painel financeiro do usuário (página de detalhe). */
    @GetMapping("/{id}")
    public String painel(@PathVariable Long id,
                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
                         Model model) {
        YearMonth ref = mes != null ? mes : YearMonth.now();
        model.addAttribute("p", painelService.montar(id, ref));
        model.addAttribute("mesAnterior", ref.minusMonths(1));
        model.addAttribute("mesSeguinte", ref.plusMonths(1));
        return "usuarios/painel";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", service.buscar(id));
        return "usuarios/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult br, RedirectAttributes ra) {
        if (!br.hasFieldErrors("email") && service.emailEmUso(usuario)) {
            br.rejectValue("email", "duplicado", "Já existe alguém com esse e-mail");
        }
        if (br.hasErrors()) return "usuarios/form";
        boolean novo = usuario.getId() == null;
        Usuario salvo = service.salvar(usuario);
        ra.addFlashAttribute("sucesso", novo ? "Bem-vindo(a), " + salvo.getNome() + "!" : "Dados atualizados.");
        return "redirect:/usuarios/" + salvo.getId();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Usuário excluído junto com suas transações, metas e tentativas.");
        return "redirect:/usuarios";
    }
}
