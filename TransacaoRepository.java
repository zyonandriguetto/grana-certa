package br.com.granacerta.repository;

import br.com.granacerta.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findAllByOrderByDataDescIdDesc();

    List<Transacao> findByUsuarioIdOrderByDataDescIdDesc(Long usuarioId);

    @Query("""
            select t from Transacao t
            where t.usuario.id = :usuarioId and t.data between :inicio and :fim
            order by t.data desc, t.id desc
            """)
    List<Transacao> doPeriodo(@Param("usuarioId") Long usuarioId,
                              @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim);
}
