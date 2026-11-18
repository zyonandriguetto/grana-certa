package br.com.granacerta.repository;

import br.com.granacerta.model.Categoria;
import br.com.granacerta.model.TipoMovimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByTipoAscNomeAsc();
    List<Categoria> findByTipoOrderByNomeAsc(TipoMovimento tipo);
}
