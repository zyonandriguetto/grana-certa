package br.com.granacerta.service;

import br.com.granacerta.model.Meta;
import br.com.granacerta.repository.MetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetaService extends CrudService<Meta> {

    private final MetaRepository metas;
    private final UsuarioService usuarioService;

    public MetaService(MetaRepository metas, UsuarioService usuarioService) {
        super(metas, "Meta");
        this.metas = metas;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Meta> listar() {
        return metas.findAllByOrderByPrazoAsc();
    }

    @Transactional(readOnly = true)
    public List<Meta> listarPorUsuario(Long usuarioId) {
        return metas.findByUsuarioIdOrderByPrazoAsc(usuarioId);
    }

    @Transactional
    public Meta salvar(Meta meta, Long usuarioId) {
        if (meta.getId() != null) {
            Meta atual = buscar(meta.getId());
            if (!atual.getUsuario().getId().equals(usuarioId) && !atual.getAportes().isEmpty()) {
                throw new RegraNegocioException("Essa meta já tem depósitos, então não pode trocar de dono.");
            }
            atual.setTitulo(meta.getTitulo());
            atual.setDescricao(meta.getDescricao());
            atual.setValorAlvo(meta.getValorAlvo());
            atual.setPrazo(meta.getPrazo());
            atual.setEmoji(meta.getEmoji());
            meta = atual;
        }
        meta.setUsuario(usuarioService.buscar(usuarioId));
        return metas.save(meta);
    }
}
