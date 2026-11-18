package br.com.granacerta.repository;

import br.com.granacerta.model.Pergunta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerguntaRepository extends JpaRepository<Pergunta, Long> {
    List<Pergunta> findAllByOrderByLicaoOrdemAscIdAsc();
    List<Pergunta> findByLicaoIdOrderByIdAsc(Long licaoId);
}
