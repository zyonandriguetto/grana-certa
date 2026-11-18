package br.com.granacerta;

import br.com.granacerta.repository.*;
import br.com.granacerta.service.SimuladorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class GranaCertaApplicationTests {

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired TransacaoRepository transacoes;
    @Autowired MetaRepository metas;
    @Autowired LicaoRepository licoes;
    @Autowired PerguntaRepository perguntas;
    @Autowired TentativaRepository tentativas;
    @Autowired SimuladorService simulador;

    @Test
    void todasAsPaginasAbrem() throws Exception {
        Long u = usuarios.findAll().get(0).getId();
        Long c = categorias.findAll().get(0).getId();
        Long t = transacoes.findAll().get(0).getId();
        Long m = metas.findAll().get(0).getId();
        Long l = licoes.findAll().get(0).getId();
        Long p = perguntas.findAll().get(0).getId();
        Long te = tentativas.findAll().get(0).getId();

        String[] urls = {
                "/", "/ranking", "/sobre",
                "/usuarios", "/usuarios/novo", "/usuarios/" + u, "/usuarios/" + u + "/editar", "/usuarios/" + u + "?mes=2020-01",
                "/categorias", "/categorias/novo", "/categorias/" + c + "/editar",
                "/transacoes", "/transacoes?usuarioId=" + u, "/transacoes/novo", "/transacoes/" + t + "/editar",
                "/metas", "/metas/novo", "/metas/" + m, "/metas/" + m + "/editar",
                "/licoes", "/licoes/novo", "/licoes/" + l, "/licoes/" + l + "/editar",
                "/perguntas", "/perguntas/novo", "/perguntas/" + p + "/editar",
                "/tentativas", "/tentativas/novo", "/tentativas/" + te + "/editar",
                "/quiz/" + l + "?usuarioId=" + u,
                "/simuladores", "/simuladores/juros-compostos", "/simuladores/cartao-rotativo", "/simuladores/parcelado-ou-avista"
        };
        for (String url : urls) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
        mvc.perform(get("/usuarios/999999")).andExpect(status().isNotFound());
    }

    @Test
    void crudDeUsuarioComValidacao() throws Exception {
        // inválido: volta para o formulário
        mvc.perform(post("/usuarios").param("nome", "").param("email", "x"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/form"));

        long antes = usuarios.count();
        mvc.perform(post("/usuarios")
                        .param("nome", "Teste").param("email", "teste@x.com")
                        .param("dataNascimento", "2008-01-01").param("rendaMensal", "500").param("avatar", "🐸"))
                .andExpect(status().is3xxRedirection());
        assertThat(usuarios.count()).isEqualTo(antes + 1);

        Long id = usuarios.findAll().stream().filter(x -> x.getEmail().equals("teste@x.com")).findFirst().orElseThrow().getId();

        // e-mail duplicado
        mvc.perform(post("/usuarios")
                        .param("nome", "Outro").param("email", "teste@x.com")
                        .param("dataNascimento", "2008-01-01").param("rendaMensal", "500"))
                .andExpect(view().name("usuarios/form"));

        // transação + meta ligadas ao usuário
        Long cat = categorias.findAll().stream().filter(x -> x.getNome().startsWith("Poupan")).findFirst().orElseThrow().getId();
        mvc.perform(post("/metas").param("usuarioId", id.toString()).param("titulo", "Bike")
                        .param("valorAlvo", "800").param("prazo", "2030-01-01").param("emoji", "🚲"))
                .andExpect(status().is3xxRedirection());
        Long meta = metas.findAll().stream().filter(x -> x.getTitulo().equals("Bike")).findFirst().orElseThrow().getId();
        mvc.perform(post("/transacoes").param("usuarioId", id.toString()).param("categoriaId", cat.toString())
                        .param("metaId", meta.toString()).param("descricao", "Depósito").param("valor", "100")
                        .param("data", "2026-01-10"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/metas/" + meta)).andExpect(status().isOk());

        // excluir usuário remove os dados ligados (cascade)
        mvc.perform(post("/usuarios/" + id + "/excluir")).andExpect(status().is3xxRedirection());
        assertThat(usuarios.existsById(id)).isFalse();
        assertThat(metas.existsById(meta)).isFalse();
    }

    @Test
    void quizGravaTentativa() throws Exception {
        Long u = usuarios.findAll().get(0).getId();
        var licao = licoes.findAllByOrderByOrdemAsc().get(0);
        var ps = perguntas.findByLicaoIdOrderByIdAsc(licao.getId());
        long antes = tentativas.count();

        var req = post("/quiz/" + licao.getId()).param("usuarioId", u.toString());
        for (var p : ps) req = req.param("resposta_" + p.getId(), p.getCorreta());
        mvc.perform(req).andExpect(status().isOk()).andExpect(view().name("quiz/resultado"));

        assertThat(tentativas.count()).isEqualTo(antes + 1);
        var ultima = tentativas.findAllByOrderByDataHoraDesc().get(0);
        assertThat(ultima.getAcertos()).isEqualTo(ps.size());
    }

    @Test
    void categoriaComTransacoesNaoPodeSerExcluida() throws Exception {
        var cat = categorias.findAll().stream().filter(x -> x.getNome().equals("Transporte")).findFirst().orElseThrow();
        mvc.perform(post("/categorias/" + cat.getId() + "/excluir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("erro"));
        assertThat(categorias.existsById(cat.getId())).isTrue();
    }

    @Test
    void simuladores() {
        // R$ 100 por 12 meses a 1% a.m. = 1268,25
        var r = simulador.jurosCompostos(BigDecimal.ZERO, new BigDecimal("100"), BigDecimal.ONE, 12);
        assertThat(r.saldoFinal()).isEqualByComparingTo("1268.25");

        // Rotativo: com o teto da lei, a dívida não passa do dobro
        var rot = simulador.rotativo(new BigDecimal("500"), new BigDecimal("15"), 24);
        assertThat(rot.comTeto()).isEqualByComparingTo("1000.00");
        assertThat(rot.semTeto()).isGreaterThan(new BigDecimal("1000"));

        // 10x sem juros com 10% de desconto à vista esconde ~1,96% a.m.
        var parc = simulador.parcelamento(new BigDecimal("1000"), new BigDecimal("10"), 10, new BigDecimal("0.8"));
        assertThat(parc.taxaImplicitaMensalPct()).isBetween(new BigDecimal("1.90"), new BigDecimal("2.00"));
        assertThat(parc.melhorAVista()).isTrue();
    }
}
