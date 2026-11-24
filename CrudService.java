package br.com.granacerta.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Operações básicas de CRUD compartilhadas por todos os serviços. */
public abstract class CrudService<T> {

    protected final JpaRepository<T, Long> repository;
    private final String nomeEntidade;

    protected CrudService(JpaRepository<T, Long> repository, String nomeEntidade) {
        this.repository = repository;
        this.nomeEntidade = nomeEntidade;
    }

    @Transactional(readOnly = true)
    public List<T> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public T buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RegistroNaoEncontradoException(nomeEntidade, id));
    }

    @Transactional
    public T salvar(T entidade) {
        return repository.save(entidade);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
    }

    public long contar() {
        return repository.count();
    }
}
