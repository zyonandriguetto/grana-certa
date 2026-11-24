package br.com.granacerta;

import br.com.granacerta.repository.*;
import br.com.granacerta.service.SimuladorService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class GranaCertaApplicationTests {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired TransacaoRepository transacoes;
    @Autowired MetaRepository metas;
    @Autowired LicaoRepository licoes;
    @Autowired PerguntaRepository perguntas;
    @Autowired TentativaRepository tentativas;
    @Autowired SimuladorService simulador;

    private static long idDe(MvcResult r) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private static String usuarioJson(String nome, String email) {
        return """
                {"nome": "%s", "email": "%s", "dataNascimento": "2008-01-01", "rendaMensal": 500, "avatar": "🐸"}
                """.formatted(nome, email);
    }

    @Test
    void todosOsEndpointsDeLeituraRespondem() throws Exception {
        Long u = usuarios.findAll().get(0).getId();
        Long c = categorias.findAll().get(0).getId();
        Long t = transacoes.findAll().get(0).getId();
        Long m = metas.findAll().get(0).getId();
        Long l = licoes.findAll().get(0).getId();
        Long p = perguntas.findAll().get(0).getId();
        Long te = tentativas.findAll().get(0).getId();

        String[] urls = {
                "/", "/api", "/api/ranking",
                "/api/usuarios", "/api/usuarios/" + u,
                "/api/usuarios/" + u + "/painel", "/api/usuarios/" + u + "/painel?mes=2020-01",
                "/api/categorias", "/api/categorias/" + c,
                "/api/transacoes", "/api/transacoes?usuarioId=" + u, "/api/transacoes/" + t,
                "/api/metas", "/api/metas?usuarioId=" + u, "/api/metas/" + m,
                "/api/licoes", "/api/licoes/" + l,
                "/api/perguntas", "/api/perguntas?licaoId=" + l, "/api/perguntas/" + p,
                "/api/tentativas", "/api/tentativas/" + te,
                "/api/quiz/" + l,
                "/api/simuladores", "/api/simuladores/juros-compostos",
                "/api/simuladores/cartao-rotativo", "/api/simuladores/parcelado-ou-avista"
        };
        for (String url : urls) {
            mvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(JSON));
        }
    }

    @Test
    void errosViramProblemDetail() throws Exception {
        mvc.perform(get("/api/usuarios/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
        // id que não é número e mês mal formatado: 400, não 500
        mvc.perform(get("/api/usuarios/abc")).andExpect(status().isBadRequest());
        Long u = usuarios.findAll().get(0).getId();
        mvc.perform(get("/api/usuarios/" + u + "/painel?mes=abc")).andExpect(status().isBadRequest());
        // JSON quebrado
        mvc.perform(post("/api/usuarios").contentType(JSON).content("{nome:"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crudDeUsuarioComValidacao() throws Exception {
        // inválido: 400 com a mensagem de cada campo
        mvc.perform(post("/api/usuarios").contentType(JSON).content("{\"nome\": \"\", \"email\": \"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nome").value("Informe o nome"))
                .andExpect(jsonPath("$.erros.email").value("E-mail inválido"));

        long antes = usuarios.count();
        MvcResult criado = mvc.perform(post("/api/usuarios").contentType(JSON)
                        .content(usuarioJson("Teste", "teste@x.com")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value("teste@x.com"))
                .andReturn();
        assertThat(usuarios.count()).isEqualTo(antes + 1);
        long id = idDe(criado);

        // e-mail duplicado
        mvc.perform(post("/api/usuarios").contentType(JSON).content(usuarioJson("Outro", "teste@x.com")))
                .andExpect(status().isConflict());

        // atualizar (mantendo o próprio e-mail não conta como duplicado)
        mvc.perform(put("/api/usuarios/" + id).contentType(JSON).content(usuarioJson("Teste 2", "teste@x.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teste 2"));

        // meta + depósito ligados ao usuário
        Long cat = categorias.findAll().stream().filter(x -> x.getNome().startsWith("Poupan"))
                .findFirst().orElseThrow().getId();
        MvcResult metaRes = mvc.perform(post("/api/metas").contentType(JSON).content("""
                        {"usuarioId": %d, "titulo": "Bike", "valorAlvo": 800, "prazo": "2030-01-01", "emoji": "🚲"}
                        """.formatted(id)))
                .andExpect(status().isCreated())
                .andReturn();
        long meta = idDe(metaRes);

        mvc.perform(post("/api/transacoes").contentType(JSON).content("""
                        {"usuarioId": %d, "categoriaId": %d, "metaId": %d,
                         "descricao": "Depósito", "valor": 100, "data": "2026-01-10"}
                        """.formatted(id, cat, meta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.metaId").value((int) meta));

        mvc.perform(get("/api/metas/" + meta))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aportes.length()").value(1))
                .andExpect(jsonPath("$.percentual").value(12));

        // excluir usuário remove os dados ligados (cascade)
        mvc.perform(delete("/api/usuarios/" + id)).andExpect(status().isNoContent());
        assertThat(usuarios.existsById(id)).isFalse();
        assertThat(metas.existsById(meta)).isFalse();
        mvc.perform(get("/api/usuarios/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void atualizarRecursoInexistenteDa404SemCriarNada() throws Exception {
        long antes = categorias.count();
        mvc.perform(put("/api/categorias/999999").contentType(JSON).content("""
                        {"nome": "Fantasma", "tipo": "DESPESA", "classificacao": "DESEJO"}
                        """))
                .andExpect(status().isNotFound());
        assertThat(categorias.count()).isEqualTo(antes);
    }

    @Test
    void quizGravaTentativa() throws Exception {
        Long u = usuarios.findAll().get(0).getId();
        var licao = licoes.findAllByOrderByOrdemAsc().get(0);
        var ps = perguntas.findByLicaoIdOrderByIdAsc(licao.getId());
        long antes = tentativas.count();

        // quem joga não recebe o gabarito
        mvc.perform(get("/api/quiz/" + licao.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perguntas.length()").value(ps.size()))
                .andExpect(jsonPath("$.perguntas[0].correta").doesNotExist())
                .andExpect(jsonPath("$.perguntas[0].alternativas.A").exists());

        StringBuilder respostas = new StringBuilder();
        for (var p : ps) {
            if (respostas.length() > 0) respostas.append(',');
            respostas.append('"').append(p.getId()).append("\": \"").append(p.getCorreta()).append('"');
        }
        mvc.perform(post("/api/quiz/" + licao.getId()).contentType(JSON)
                        .content("{\"usuarioId\": " + u + ", \"respostas\": {" + respostas + "}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tentativa.acertos").value(ps.size()))
                .andExpect(jsonPath("$.correcoes[0].acertou").value(true));

        assertThat(tentativas.count()).isEqualTo(antes + 1);
        var ultima = tentativas.findAllByOrderByDataHoraDesc().get(0);
        assertThat(ultima.getAcertos()).isEqualTo(ps.size());

        // corpo sem os campos obrigatórios
        mvc.perform(post("/api/quiz/" + licao.getId()).contentType(JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.usuarioId").exists());
    }

    @Test
    void regrasDeNegocio() throws Exception {
        // categoria com transações não pode ser excluída
        var cat = categorias.findAll().stream().filter(x -> x.getNome().equals("Transporte"))
                .findFirst().orElseThrow();
        mvc.perform(delete("/api/categorias/" + cat.getId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").exists());
        assertThat(categorias.existsById(cat.getId())).isTrue();

        // receita só pode ter classificação "Entrada"
        mvc.perform(post("/api/categorias").contentType(JSON).content("""
                        {"nome": "Errada", "tipo": "RECEITA", "classificacao": "DESEJO"}
                        """))
                .andExpect(status().isUnprocessableEntity());

        // acertos não pode passar do total de perguntas
        Long u = usuarios.findAll().get(0).getId();
        Long l = licoes.findAll().get(0).getId();
        mvc.perform(post("/api/tentativas").contentType(JSON).content("""
                        {"usuarioId": %d, "licaoId": %d, "acertos": 5, "totalPerguntas": 3,
                         "dataHora": "2026-01-10T10:30:00"}
                        """.formatted(u, l)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void simuladores() throws Exception {
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

        // mesma conta pelo endpoint, que também devolve os parâmetros usados
        mvc.perform(get("/api/simuladores/juros-compostos?inicial=0&mensal=100&taxa=1&meses=12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meses").value(12))
                .andExpect(jsonPath("$.resultado.saldoFinal").value(1268.25));
        // meses fora do limite é ajustado
        mvc.perform(get("/api/simuladores/juros-compostos?meses=99999"))
                .andExpect(jsonPath("$.meses").value(600));
    }
}
