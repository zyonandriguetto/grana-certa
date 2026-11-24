package br.com.granacerta.dto;

import br.com.granacerta.model.Tentativa;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public final class TentativaDto {

    private TentativaDto() {}

    public record Request(
            @NotNull(message = "Escolha o usuário") Long usuarioId,
            @NotNull(message = "Escolha a lição") Long licaoId,
            @Min(value = 0, message = "Não pode ser negativo") int acertos,
            @Min(value = 1, message = "Precisa ter ao menos 1 pergunta") int totalPerguntas,
            @NotNull(message = "Informe a data") LocalDateTime dataHora) {

        /** Aplica só os campos próprios; usuário e lição são ligados pelo service. */
        public void aplicarEm(Tentativa t) {
            t.setAcertos(acertos);
            t.setTotalPerguntas(totalPerguntas);
            t.setDataHora(dataHora);
        }
    }

    public record Response(Long id, Long usuarioId, String usuarioNome, Long licaoId, String licaoTitulo,
                           int acertos, int totalPerguntas, int percentual, int pontos, LocalDateTime dataHora) {

        public static Response de(Tentativa t) {
            return new Response(t.getId(), t.getUsuario().getId(), t.getUsuario().getNome(),
                    t.getLicao().getId(), t.getLicao().getTitulo(), t.getAcertos(), t.getTotalPerguntas(),
                    t.getPercentual(), t.getPontos(), t.getDataHora());
        }
    }
}
