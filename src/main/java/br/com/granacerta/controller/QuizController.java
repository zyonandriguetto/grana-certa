package br.com.granacerta.controller;

import br.com.granacerta.dto.LicaoDto;
import br.com.granacerta.dto.QuizDto;
import br.com.granacerta.dto.TentativaDto;
import br.com.granacerta.model.Licao;
import br.com.granacerta.model.Pergunta;
import br.com.granacerta.model.Tentativa;
import br.com.granacerta.service.LicaoService;
import br.com.granacerta.service.PerguntaService;
import br.com.granacerta.service.RegraNegocioException;
import br.com.granacerta.service.TentativaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Jogo de perguntas: o usuário responde e a tentativa é gravada no banco. */
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final LicaoService licoes;
    private final PerguntaService perguntas;
    private final TentativaService tentativas;

    public QuizController(LicaoService licoes, PerguntaService perguntas, TentativaService tentativas) {
        this.licoes = licoes;
        this.perguntas = perguntas;
        this.tentativas = tentativas;
    }

    /** Perguntas da lição, sem o gabarito. */
    @GetMapping("/{licaoId}")
    public QuizDto.Quiz jogar(@PathVariable Long licaoId) {
        Licao licao = licoes.buscar(licaoId);
        List<Pergunta> lista = perguntas.listarPorLicao(licaoId);
        if (lista.isEmpty()) throw new RegraNegocioException("Essa lição ainda não tem perguntas cadastradas.");
        return new QuizDto.Quiz(LicaoDto.Response.de(licao), lista.stream().map(QuizDto.PerguntaQuiz::de).toList());
    }

    /** Corrige o quiz, grava a tentativa e devolve o gabarito comentado. */
    @PostMapping("/{licaoId}")
    public QuizDto.Resultado corrigir(@PathVariable Long licaoId, @Valid @RequestBody QuizDto.Request req) {
        licoes.buscar(licaoId); // 404 se a lição não existe
        Tentativa t = tentativas.corrigirQuiz(req.usuarioId(), licaoId, req.respostas());

        List<QuizDto.Correcao> correcoes = perguntas.listarPorLicao(licaoId).stream()
                .map(p -> QuizDto.Correcao.de(p, req.respostas().get(p.getId())))
                .toList();
        LicaoDto.Response proxima = licoes.listar().stream()
                .filter(l -> l.getOrdem() > t.getLicao().getOrdem())
                .findFirst()
                .map(LicaoDto.Response::de)
                .orElse(null);
        return new QuizDto.Resultado(TentativaDto.Response.de(t), correcoes, proxima);
    }
}
