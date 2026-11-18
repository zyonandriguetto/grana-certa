package br.com.granacerta.controller;

import br.com.granacerta.model.Meta;
import br.com.granacerta.service.MetaService;
import br.com.granacerta.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/metas")
public class MetaController {

    private final MetaService service;
    private final UsuarioService usuarios;

    public MetaController(MetaService service, UsuarioService usuarios) {
        this.service = service;
        this.usuarios = usuarios;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long usuarioId, Model model) {
        model.addAttribute("metas", usuarioId == null ? service.listar() : service.listarPorUsuario(usuarioId));
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("usuarioId", usuarioId);
        return "metas/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("meta", service.buscar(id));
        return "metas/detalhe";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long usuarioId, Model model) {
        model.addAttribute("meta", new Meta());
        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute("usuarios", usuarios.listar());
        return "metas/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Meta m = service.buscar(id);
        model.addAttribute("meta", m);
        model.addAttribute("usuarioId", m.getUsuario().getId());
        model.addAttribute("usuarios", usuarios.listar());
        return "metas/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("meta") Meta meta, BindingResult br,
                         @RequestParam(required = false) Long usuarioId, Model model, RedirectAttributes ra) {
        if (usuarioId == null) br.reject("usuario", "Escolha o usuário dono da meta");
        if (br.hasErrors()) {
            model.addAttribute("usuarioId", usuarioId);
            model.addAttribute("usuarios", usuarios.listar());
            return "metas/form";
        }
        Meta salva = service.salvar(meta, usuarioId);
        ra.addFlashAttribute("sucesso", "Meta \"" + salva.getTitulo() + "\" salva.");
        return "redirect:/metas/" + salva.getId();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Meta excluída. Os depósitos continuam no histórico de transações.");
        return "redirect:/metas";
    }
}
