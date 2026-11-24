package br.com.granacerta.dto;

import br.com.granacerta.model.Categoria;
import br.com.granacerta.model.Classificacao;
import br.com.granacerta.model.TipoMovimento;
import jakarta.validation.constraints.*;

public final class CategoriaDto {

    private CategoriaDto() {}

    public record Request(
            @NotBlank(message = "Informe o nome") @Size(max = 60) String nome,
            @NotNull(message = "Escolha o tipo") TipoMovimento tipo,
            @NotNull(message = "Escolha a classificação") Classificacao classificacao,
            @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Cor no formato #RRGGBB") String cor,
            @Size(max = 40) String icone) {

        public void aplicarEm(Categoria c) {
            c.setNome(nome);
            c.setTipo(tipo);
            c.setClassificacao(classificacao);
            if (cor != null) c.setCor(cor);
            if (icone != null) c.setIcone(icone);
        }
    }

    public record Response(Long id, String nome, TipoMovimento tipo, Classificacao classificacao,
                           String cor, String icone) {

        public static Response de(Categoria c) {
            return new Response(c.getId(), c.getNome(), c.getTipo(), c.getClassificacao(), c.getCor(), c.getIcone());
        }
    }
}
