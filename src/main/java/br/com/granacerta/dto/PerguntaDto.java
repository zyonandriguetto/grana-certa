package br.com.granacerta.dto;

import br.com.granacerta.model.Pergunta;
import jakarta.validation.constraints.*;

public final class PerguntaDto {

    private PerguntaDto() {}

    public record Request(
            @NotBlank(message = "Escreva o enunciado") @Size(max = 500) String enunciado,
            @NotBlank(message = "Preencha a alternativa A") @Size(max = 255) String alternativaA,
            @NotBlank(message = "Preencha a alternativa B") @Size(max = 255) String alternativaB,
            @NotBlank(message = "Preencha a alternativa C") @Size(max = 255) String alternativaC,
            @NotBlank(message = "Preencha a alternativa D") @Size(max = 255) String alternativaD,
            @NotBlank(message = "Escolha a alternativa correta") @Pattern(regexp = "[ABCD]", message = "Use A, B, C ou D") String correta,
            @Size(max = 500) String explicacao,
            @NotNull(message = "Escolha a lição") Long licaoId) {

        /** Aplica só os campos próprios; a lição é ligada pelo service. */
        public void aplicarEm(Pergunta p) {
            p.setEnunciado(enunciado);
            p.setAlternativaA(alternativaA);
            p.setAlternativaB(alternativaB);
            p.setAlternativaC(alternativaC);
            p.setAlternativaD(alternativaD);
            p.setCorreta(correta);
            p.setExplicacao(explicacao);
        }
    }

    public record Response(Long id, String enunciado, String alternativaA, String alternativaB,
                           String alternativaC, String alternativaD, String correta, String explicacao,
                           Long licaoId) {

        public static Response de(Pergunta p) {
            return new Response(p.getId(), p.getEnunciado(), p.getAlternativaA(), p.getAlternativaB(),
                    p.getAlternativaC(), p.getAlternativaD(), p.getCorreta(), p.getExplicacao(),
                    p.getLicao().getId());
        }
    }
}
