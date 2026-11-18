package br.com.granacerta.service;

import br.com.granacerta.model.Pergunta;
import br.com.granacerta.repository.PerguntaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PerguntaService extends CrudService<Pergunta> {

    private final PerguntaRepository perguntas;
    private final LicaoService licaoService;

    public PerguntaService(PerguntaRepository perguntas, LicaoService licaoService) {
        super(perguntas, "Pergunta");
        this.perguntas = perguntas;
        this.licaoService = licaoService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pergunta> listar() {
        return perguntas.findAllByOrderByLicaoOrdemAscIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Pergunta> listarPorLicao(Long licaoId) {
        return perguntas.findByLicaoIdOrderByIdAsc(licaoId);
    }

    @Transactional
    public Pergunta salvar(Pergunta p, Long licaoId) {
        p.setLicao(licaoService.buscar(licaoId));
        return perguntas.save(p);
    }
}
