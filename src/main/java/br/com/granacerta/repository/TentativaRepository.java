package br.com.granacerta.repository;

import br.com.granacerta.model.Tentativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TentativaRepository extends JpaRepository<Tentativa, Long> {
    List<Tentativa> findAllByOrderByDataHoraDesc();
}
