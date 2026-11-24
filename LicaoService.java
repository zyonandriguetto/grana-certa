package br.com.granacerta.service;

import br.com.granacerta.model.Licao;
import br.com.granacerta.repository.LicaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LicaoService extends CrudService<Licao> {

    private final LicaoRepository licoes;

    public LicaoService(LicaoRepository licoes) {
        super(licoes, "Lição");
        this.licoes = licoes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Licao> listar() {
        return licoes.findAllByOrderByOrdemAsc();
    }

    @Override
    @Transactional
    public Licao salvar(Licao l) {
        if (l.getId() != null) {
            // mantém perguntas e tentativas já vinculadas
            Licao atual = buscar(l.getId());
            atual.setTitulo(l.getTitulo());
            atual.setTema(l.getTema());
            atual.setResumo(l.getResumo());
            atual.setConteudo(l.getConteudo());
            atual.setNivel(l.getNivel());
            atual.setFonte(l.getFonte());
            atual.setIcone(l.getIcone());
            atual.setOrdem(l.getOrdem());
            return licoes.save(atual);
        }
        return licoes.save(l);
    }
}
