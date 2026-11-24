package br.com.granacerta.dto;

import br.com.granacerta.model.Pergunta;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/** Contratos do jogo de perguntas. */
public final class QuizDto {

    private QuizDto() {}

    /** Pergunta como o jogador vê: sem a resposta certa nem a explicação. */
    public record PerguntaQuiz(Long id, String enunciado, Map<String, String> alternativas) {
        public static PerguntaQuiz de(Pergunta p) {
            return new PerguntaQuiz(p.getId(), p.getEnunciado(), p.getAlternativas());
        }
    }

    public record Quiz(LicaoDto.Response licao, List<PerguntaQuiz> perguntas) {}

    /** respostas = { idDaPergunta: "A" | "B" | "C" | "D" } */
    public record Request(
            @NotNull(message = "Informe o usuário") Long usuarioId,
            @NotNull(message = "Envie as respostas") Map<Long, String> respostas) {}

    /** Correção de uma pergunta, já revelando o gabarito. */
    public record Correcao(Long perguntaId, String enunciado, String marcada, String correta,
                           String textoCorreto, boolean acertou, String explicacao) {

        public static Correcao de(Pergunta p, String marcada) {
            boolean acertou = p.getCorreta().equalsIgnoreCase(marcada == null ? "" : marcada);
            return new Correcao(p.getId(), p.getEnunciado(), marcada, p.getCorreta(), p.getTextoCorreto(),
                    acertou, p.getExplicacao());
        }
    }

    public record Resultado(TentativaDto.Response tentativa, List<Correcao> correcoes,
                            LicaoDto.Response proximaLicao) {}
}
