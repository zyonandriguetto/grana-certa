package br.com.granacerta.dto;

import br.com.granacerta.model.Meta;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class MetaDto {

    private MetaDto() {}

    public record Request(
            @NotBlank(message = "Dê um nome para a meta") @Size(max = 80) String titulo,
            @Size(max = 255) String descricao,
            @NotNull(message = "Informe o valor da meta") @Positive(message = "O valor precisa ser maior que zero") BigDecimal valorAlvo,
            @NotNull(message = "Informe o prazo") LocalDate prazo,
            @Size(max = 10) String emoji,
            @NotNull(message = "Escolha o usuário dono da meta") Long usuarioId) {

        /** Aplica só os campos próprios; o dono é ligado pelo service. */
        public void aplicarEm(Meta m) {
            m.setTitulo(titulo);
            m.setDescricao(descricao);
            m.setValorAlvo(valorAlvo);
            m.setPrazo(prazo);
            if (emoji != null) m.setEmoji(emoji);
        }
    }

    public record Response(Long id, String titulo, String descricao, BigDecimal valorAlvo, LocalDate prazo,
                           String emoji, Long usuarioId, BigDecimal valorAtual, int percentual, boolean concluida,
                           BigDecimal aporteMensalSugerido, long diasRestantes,
                           List<TransacaoDto.Response> aportes) {

        public static Response de(Meta m) {
            return new Response(m.getId(), m.getTitulo(), m.getDescricao(), m.getValorAlvo(), m.getPrazo(),
                    m.getEmoji(), m.getUsuario().getId(), m.getValorAtual(), m.getPercentual(), m.isConcluida(),
                    m.getAporteMensalSugerido(), m.getDiasRestantes(),
                    m.getAportes().stream().map(TransacaoDto.Response::de).toList());
        }
    }
}
