package br.com.granacerta.service;

import br.com.granacerta.model.Meta;
import br.com.granacerta.model.Transacao;
import br.com.granacerta.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransacaoService extends CrudService<Transacao> {

    private final TransacaoRepository transacoes;
    private final UsuarioService usuarioService;
    private final CategoriaService categoriaService;
    private final MetaService metaService;

    public TransacaoService(TransacaoRepository transacoes, UsuarioService usuarioService,
                            CategoriaService categoriaService, MetaService metaService) {
        super(transacoes, "Transação");
        this.transacoes = transacoes;
        this.usuarioService = usuarioService;
        this.categoriaService = categoriaService;
        this.metaService = metaService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transacao> listar() {
        return transacoes.findAllByOrderByDataDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Transacao> listarPorUsuario(Long usuarioId) {
        return transacoes.findByUsuarioIdOrderByDataDescIdDesc(usuarioId);
    }

    /** Liga a transação ao usuário, categoria e (opcionalmente) meta antes de salvar. */
    @Transactional
    public Transacao salvar(Transacao t, Long usuarioId, Long categoriaId, Long metaId) {
        t.setUsuario(usuarioService.buscar(usuarioId));
        t.setCategoria(categoriaService.buscar(categoriaId));
        if (metaId != null) {
            Meta meta = metaService.buscar(metaId);
            if (!meta.getUsuario().getId().equals(usuarioId)) {
                throw new RegraNegocioException("A meta escolhida pertence a outro usuário.");
            }
            if (t.isReceita()) {
                throw new RegraNegocioException("Depósitos em meta precisam usar uma categoria de despesa (ex.: Poupança).");
            }
            t.setMeta(meta);
        } else {
            t.setMeta(null);
        }
        return transacoes.save(t);
    }
}
