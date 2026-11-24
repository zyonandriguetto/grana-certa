package br.com.granacerta.controller;

import br.com.granacerta.dto.UsuarioDto;
import br.com.granacerta.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

    public record Totais(long usuarios, long categorias, long transacoes, long metas,
                         long licoes, long perguntas, long tentativas) {}

    public record Resumo(String aplicacao, String descricao, String fonte, Totais totais) {}

    private final UsuarioService usuarios;
    private final CategoriaService categorias;
    private final TransacaoService transacoes;
    private final MetaService metas;
    private final LicaoService licoes;
    private final PerguntaService perguntas;
    private final TentativaService tentativas;

    public HomeController(UsuarioService usuarios, CategoriaService categorias, TransacaoService transacoes,
                          MetaService metas, LicaoService licoes, PerguntaService perguntas,
                          TentativaService tentativas) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.transacoes = transacoes;
        this.metas = metas;
        this.licoes = licoes;
        this.perguntas = perguntas;
        this.tentativas = tentativas;
    }

    /** Cartão de visita da API: o que é e quantos registros existem. Serve também de health check. */
    @GetMapping({"/", "/api"})
    public Resumo inicio() {
        Totais totais = new Totais(usuarios.contar(), categorias.contar(), transacoes.contar(), metas.contar(),
                licoes.contar(), perguntas.contar(), tentativas.contar());
        return new Resumo("GranaCerta", "API de educação financeira para jovens",
                "https://www.bcb.gov.br/meubc/faqs", totais);
    }

    /** Usuários ordenados pela pontuação nos quizzes (maior primeiro). */
    @GetMapping("/api/ranking")
    public List<UsuarioDto.Response> ranking() {
        return usuarios.ranking().stream().map(UsuarioDto.Response::de).toList();
    }
}
