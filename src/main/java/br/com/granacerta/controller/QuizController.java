package br.com.granacerta.controller;

import br.com.granacerta.model.Licao;
import br.com.granacerta.model.Pergunta;
import br.com.granacerta.model.Tentativa;
import br.com.granacerta.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Jogo de perguntas: o usuário responde e a tentativa é gravada no banco. */
@Controller
@RequestMapping("/quiz")
public class QuizController {

    private final LicaoService licoes;
    private final PerguntaService perguntas;
    private final UsuarioService usuarios;
    private final TentativaService tentativas;

    public QuizController(LicaoService licoes, PerguntaService perguntas,
                          UsuarioService usuarios, TentativaService tentativas) {
        this.licoes = licoes;
        this.perguntas = perguntas;
        this.usuarios = usuarios;
        this.tentativas = tentativas;
    }

    @GetMapping("/{licaoId}")
    public String jogar(@PathVariable Long licaoId, @RequestParam Long usuarioId, Model model) {
        Licao licao = licoes.buscar(licaoId);
        List<Pergunta> lista = perguntas.listarPorLicao(licaoId);
        if (lista.isEmpty()) throw new RegraNegocioException("Essa lição ainda não tem perguntas cadastradas.");
        model.addAttribute("licao", licao);
        model.addAttribute("perguntas", lista);
        model.addAttribute("usuario", usuarios.buscar(usuarioId));
        return "quiz/jogar";
    }

    @PostMapping("/{licaoId}")
    public String corrigir(@PathVariable Long licaoId, @RequestParam Long usuarioId,
                           @RequestParam Map<String, String> params, Model model) {
        // campos chegam como resposta_{idPergunta}=A
        Map<Long, String> respostas = new HashMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("resposta_")) {
                try {
                    respostas.put(Long.parseLong(k.substring("resposta_".length())), v);
                } catch (NumberFormatException ignored) {
                    // campo inválido é ignorado
                }
            }
        });
        Tentativa t = tentativas.corrigirQuiz(usuarioId, licaoId, respostas);
        model.addAttribute("tentativa", t);
        model.addAttribute("perguntas", perguntas.listarPorLicao(licaoId));
        model.addAttribute("respostas", respostas);
        model.addAttribute("proxima", licoes.listar().stream()
                .filter(l -> l.getOrdem() > t.getLicao().getOrdem()).findFirst().orElse(null));
        return "quiz/resultado";
    }
}
