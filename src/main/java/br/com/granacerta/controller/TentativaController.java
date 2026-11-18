package br.com.granacerta.controller;

import br.com.granacerta.model.Tentativa;
import br.com.granacerta.service.LicaoService;
import br.com.granacerta.service.TentativaService;
import br.com.granacerta.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** CRUD administrativo das tentativas (o fluxo normal é responder o quiz). */
@Controller
@RequestMapping("/tentativas")
public class TentativaController {

    private final TentativaService service;
    private final UsuarioService usuarios;
    private final LicaoService licoes;

    public TentativaController(TentativaService service, UsuarioService usuarios, LicaoService licoes) {
        this.service = service;
        this.usuarios = usuarios;
        this.licoes = licoes;
    }

    private void opcoes(Model model) {
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("licoes", licoes.listar());
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tentativas", service.listar());
        return "tentativas/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tentativa", new Tentativa());
        model.addAttribute("usuarioId", null);
        model.addAttribute("licaoId", null);
        opcoes(model);
        return "tentativas/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Tentativa t = service.buscar(id);
        model.addAttribute("tentativa", t);
        model.addAttribute("usuarioId", t.getUsuario().getId());
        model.addAttribute("licaoId", t.getLicao().getId());
        opcoes(model);
        return "tentativas/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("tentativa") Tentativa tentativa, BindingResult br,
                         @RequestParam(required = false) Long usuarioId,
                         @RequestParam(required = false) Long licaoId,
                         Model model, RedirectAttributes ra) {
        if (usuarioId == null) br.reject("usuario", "Escolha o usuário");
        if (licaoId == null) br.reject("licao", "Escolha a lição");
        if (br.hasErrors()) {
            model.addAttribute("usuarioId", usuarioId);
            model.addAttribute("licaoId", licaoId);
            opcoes(model);
            return "tentativas/form";
        }
        service.salvar(tentativa, usuarioId, licaoId);
        ra.addFlashAttribute("sucesso", "Tentativa salva.");
        return "redirect:/tentativas";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Tentativa excluída.");
        return "redirect:/tentativas";
    }
}
