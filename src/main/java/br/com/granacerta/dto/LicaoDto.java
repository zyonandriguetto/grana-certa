package br.com.granacerta.dto;

import br.com.granacerta.model.Licao;
import br.com.granacerta.model.Nivel;
import jakarta.validation.constraints.*;

public final class LicaoDto {

    private LicaoDto() {}

    public record Request(
            @NotBlank(message = "Informe o título") @Size(max = 100) String titulo,
            @NotBlank(message = "Informe o tema") @Size(max = 60) String tema,
            @NotBlank(message = "Escreva um resumo") @Size(max = 255) String resumo,
            @NotBlank(message = "Escreva o conteúdo") String conteudo,
            @NotNull(message = "Escolha o nível") Nivel nivel,
            @Size(max = 255) String fonte,
            @Size(max = 40) String icone,
            @Min(value = 1, message = "A ordem começa em 1") Integer ordem) {

        public void aplicarEm(Licao l) {
            l.setTitulo(titulo);
            l.setTema(tema);
            l.setResumo(resumo);
            l.setConteudo(conteudo);
            l.setNivel(nivel);
            l.setFonte(fonte);
            if (icone != null) l.setIcone(icone);
            if (ordem != null) l.setOrdem(ordem);
        }
    }

    public record Response(Long id, String titulo, String tema, String resumo, String conteudo, Nivel nivel,
                           String fonte, String icone, int ordem) {

        public static Response de(Licao l) {
            return new Response(l.getId(), l.getTitulo(), l.getTema(), l.getResumo(), l.getConteudo(),
                    l.getNivel(), l.getFonte(), l.getIcone(), l.getOrdem());
        }
    }
}
