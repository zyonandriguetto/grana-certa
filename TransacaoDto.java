package br.com.granacerta.dto;

import br.com.granacerta.model.Transacao;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TransacaoDto {

    private TransacaoDto() {}

    public record Request(
            @NotBlank(message = "Descreva a transação") @Size(max = 120) String descricao,
            @NotNull(message = "Informe o valor") @Positive(message = "O valor precisa ser maior que zero") BigDecimal valor,
            @NotNull(message = "Informe a data") LocalDate data,
            boolean impulsiva,
            @NotNull(message = "Escolha o usuário") Long usuarioId,
            @NotNull(message = "Escolha a categoria") Long categoriaId,
            Long metaId) {

        /** Aplica só os campos próprios; usuário, categoria e meta são ligados pelo service. */
        public void aplicarEm(Transacao t) {
            t.setDescricao(descricao);
            t.setValor(valor);
            t.setData(data);
            t.setImpulsiva(impulsiva);
        }
    }

    public record Response(Long id, String descricao, BigDecimal valor, LocalDate data, boolean impulsiva,
                           boolean receita, Long usuarioId, CategoriaDto.Response categoria, Long metaId) {

        public static Response de(Transacao t) {
            return new Response(t.getId(), t.getDescricao(), t.getValor(), t.getData(), t.isImpulsiva(),
                    t.isReceita(), t.getUsuario().getId(), CategoriaDto.Response.de(t.getCategoria()),
                    t.getMeta() != null ? t.getMeta().getId() : null);
        }
    }
}
