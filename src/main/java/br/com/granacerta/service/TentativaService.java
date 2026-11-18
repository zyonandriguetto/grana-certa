package br.com.granacerta.service;

import br.com.granacerta.model.Pergunta;
import br.com.granacerta.model.Tentativa;
import br.com.granacerta.repository.TentativaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TentativaService extends CrudService<Tentativa> {

    private final TentativaRepository tentativas;
    private final UsuarioService usuarioService;
    private final LicaoService licaoService;
    private final PerguntaService perguntaService;

    public TentativaService(TentativaRepository tentativas, UsuarioService usuarioService,
                            LicaoService licaoService, PerguntaService perguntaService) {
        super(tentativas, "Tentativa");
        this.tentativas = tentativas;
        this.usuarioService = usuarioService;
        this.licaoService = licaoService;
        this.perguntaService = perguntaService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tentativa> listar() {
        return tentativas.findAllByOrderByDataHoraDesc();
    }

    @Transactional
    public Tentativa salvar(Tentativa t, Long usuarioId, Long licaoId) {
        t.setUsuario(usuarioService.buscar(usuarioId));
        t.setLicao(licaoService.buscar(licaoId));
        return tentativas.save(t);
    }

    /** Corrige o quiz: respostas = {idPergunta -> letra marcada}. */
    @Transactional
    public Tentativa corrigirQuiz(Long usuarioId, Long licaoId, Map<Long, String> respostas) {
        List<Pergunta> perguntas = perguntaService.listarPorLicao(licaoId);
        if (perguntas.isEmpty()) {
            throw new RegraNegocioException("Essa lição ainda não tem perguntas.");
        }
        int acertos = (int) perguntas.stream()
                .filter(p -> p.getCorreta().equalsIgnoreCase(respostas.getOrDefault(p.getId(), "")))
                .count();
        Tentativa t = new Tentativa();
        t.setAcertos(acertos);
        t.setTotalPerguntas(perguntas.size());
        return salvar(t, usuarioId, licaoId);
    }
}
