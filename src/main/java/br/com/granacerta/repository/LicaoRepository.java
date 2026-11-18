package br.com.granacerta.repository;

import br.com.granacerta.model.Licao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LicaoRepository extends JpaRepository<Licao, Long> {
    List<Licao> findAllByOrderByOrdemAsc();
}
