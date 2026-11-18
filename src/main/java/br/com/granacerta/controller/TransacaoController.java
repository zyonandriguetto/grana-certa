package br.com.granacerta.controller;

import br.com.granacerta.model.Transacao;
import br.com.granacerta.service.CategoriaService;
import br.com.granacerta.service.MetaService;
import br.com.granacerta.service.TransacaoService;
import br.com.granacerta.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;
    private final UsuarioService usuarios;
    private final CategoriaService categorias;
    private final MetaService metas;

    public TransacaoController(TransacaoService service, UsuarioService usuarios,
                               CategoriaService categorias, MetaService metas) {
        this.service = service;
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.metas = metas;
    }

    private void opcoes(Model model) {
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("categorias", categorias.listar());
        model.addAttribute("metas", metas.listar());
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long usuarioId, Model model) {
        model.addAttribute("transacoes", usuarioId == null ? service.listar() : service.listarPorUsuario(usuarioId));
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("usuarioId", usuarioId);
        return "transacoes/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long usuarioId,
                       @RequestParam(required = false) Long metaId, Model model) {
        model.addAttribute("transacao", new Transacao());
        model.addAttribute("usuarioId", usuarioId);
        model.addAttribute("categoriaId", null);
        model.addAttribute("metaId", metaId);
        opcoes(model);
        return "transacoes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Transacao t = service.buscar(id);
        model.addAttribute("transacao", t);
        model.addAttribute("usuarioId", t.getUsuario().getId());
        model.addAttribute("categoriaId", t.getCategoria().getId());
        model.addAttribute("metaId", t.getMeta() != null ? t.getMeta().getId() : null);
        opcoes(model);
        return "transacoes/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("transacao") Transacao transacao, BindingResult br,
                         @RequestParam(required = false) Long usuarioId,
                         @RequestParam(required = false) Long categoriaId,
                         @RequestParam(required = false) Long metaId,
                         @RequestParam(required = false) String voltarPara,
                         Model model, RedirectAttributes ra) {
        if (usuarioId == null) br.reject("usuario", "Escolha o usuário");
        if (categoriaId == null) br.reject("categoria", "Escolha a categoria");
        if (br.hasErrors()) {
            model.addAttribute("usuarioId", usuarioId);
            model.addAttribute("categoriaId", categoriaId);
            model.addAttribute("metaId", metaId);
            opcoes(model);
            return "transacoes/form";
        }
        service.salvar(transacao, usuarioId, categoriaId, metaId);
        ra.addFlashAttribute("sucesso", "Transação \"" + transacao.getDescricao() + "\" salva.");
        return "redirect:" + destinoSeguro(voltarPara, "/transacoes");
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, @RequestParam(required = false) String voltarPara, RedirectAttributes ra) {
        service.excluir(id);
        ra.addFlashAttribute("sucesso", "Transação excluída.");
        return "redirect:" + destinoSeguro(voltarPara, "/transacoes");
    }

    /** Só aceita caminhos internos para evitar redirecionamento aberto. */
    static String destinoSeguro(String destino, String padrao) {
        if (destino != null && destino.startsWith("/") && !destino.startsWith("//") && !destino.contains("\\")) {
            return destino;
        }
        return padrao;
    }
}
