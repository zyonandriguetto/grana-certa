package br.com.granacerta.controller;

import br.com.granacerta.dto.CategoriaDto;
import br.com.granacerta.model.Categoria;
import br.com.granacerta.model.Classificacao;
import br.com.granacerta.model.TipoMovimento;
import br.com.granacerta.service.CategoriaService;
import br.com.granacerta.service.RegraNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaDto.Response> listar() {
        return service.listar().stream().map(CategoriaDto.Response::de).toList();
    }

    @GetMapping("/{id}")
    public CategoriaDto.Response buscar(@PathVariable Long id) {
        return CategoriaDto.Response.de(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaDto.Response> criar(@Valid @RequestBody CategoriaDto.Request req) {
        validarClassificacao(req);
        Categoria c = new Categoria();
        req.aplicarEm(c);
        Categoria salva = service.salvar(c);
        return Respostas.criado(salva.getId(), CategoriaDto.Response.de(salva));
    }

    @PutMapping("/{id}")
    public CategoriaDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDto.Request req) {
        validarClassificacao(req);
        Categoria c = service.buscar(id);
        req.aplicarEm(c);
        return CategoriaDto.Response.de(service.salvar(c));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    /** Receita só pode ser "Entrada de dinheiro"; despesa nunca. */
    private static void validarClassificacao(CategoriaDto.Request req) {
        if (req.tipo() == TipoMovimento.RECEITA && req.classificacao() != null
                && req.classificacao() != Classificacao.ENTRADA) {
            throw new RegraNegocioException("Receitas usam a classificação \"Entrada de dinheiro\"");
        }
        if (req.tipo() == TipoMovimento.DESPESA && req.classificacao() == Classificacao.ENTRADA) {
            throw new RegraNegocioException("Despesas devem ser Necessidade, Desejo ou Poupança");
        }
    }
}
