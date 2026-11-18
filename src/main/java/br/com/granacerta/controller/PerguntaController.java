package br.com.granacerta.controller;

import br.com.granacerta.model.Pergunta;
import br.com.granacerta.service.LicaoService;
import br.com.granacerta.service.PerguntaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perguntas")
public class PerguntaController {

    private final PerguntaService service;
    private final LicaoService licoes;

    public PerguntaController(PerguntaService service, LicaoService licoes) {
        this.service = service;
        this.licoes = licoes;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long licaoId, Model model) {
        model.addAttribute("perguntas", licaoId == null ? service.listar() : service.listarPorLicao(licaoId));
        model.addAttribute("licoes", licoes.listar());
        model.addAttribute("licaoId", licaoId);
        return "perguntas/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long licaoId, Model model) {
        Pergunta p = new Pergunta();
        p.setCorreta("A");
        model.addAttribute("pergunta", p);
        model.addAttribute("licaoId", licaoId);
        model.addAttribute("licoes", licoes.listar());
        return "perguntas/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Pergunta p = service.buscar(id);
        model.addAttribute("pergunta", p);
        model.addAttribute("licaoId", p.getLicao().getId());
        model.addAttribute("licoes", licoes.listar());
        return "perguntas/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("pergunta") Pergunta pergunta, BindingResult br,
                         @RequestParam(required = false) Long licaoId, Model model, RedirectAttributes ra) {
        if (licaoId == null) br.reject("licao", "Escolha a lição");
        if (br.hasErrors()) {
            model.addAttribute("licaoId", licaoId);
            model.addAttribute("licoes", licoes.listar());
            return "perguntas/form";
        }
        service.salvar(pergunta, licaoId);
        ra.addFlashAttribute("sucesso", "Pergunta salva.");
        return "redirect:/perguntas?licaoId=" + licaoId;
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        Long licaoId = service.buscar(id).getLicao().getId();
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Pergunta excluída.");
        return "redirect:/perguntas?licaoId=" + licaoId;
    }
}
