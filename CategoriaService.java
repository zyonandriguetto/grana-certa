package br.com.granacerta.service;

import br.com.granacerta.model.Categoria;
import br.com.granacerta.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService extends CrudService<Categoria> {

    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        super(categorias, "Categoria");
        this.categorias = categorias;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listar() {
        return categorias.findAllByOrderByTipoAscNomeAsc();
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Categoria c = buscar(id);
        if (!c.getTransacoes().isEmpty()) {
            throw new RegraNegocioException("A categoria \"" + c.getNome() + "\" tem " + c.getTransacoes().size()
                    + " transação(ões). Mova ou exclua essas transações antes.");
        }
        categorias.delete(c);
    }
}
