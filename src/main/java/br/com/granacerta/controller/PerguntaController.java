package br.com.granacerta.controller;

import br.com.granacerta.dto.PerguntaDto;
import br.com.granacerta.model.Pergunta;
import br.com.granacerta.service.PerguntaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perguntas")
public class PerguntaController {

    private final PerguntaService service;

    public PerguntaController(PerguntaService service) {
        this.service = service;
    }

    /** Filtro opcional: ?licaoId= */
    @GetMapping
    public List<PerguntaDto.Response> listar(@RequestParam(required = false) Long licaoId) {
        List<Pergunta> lista = licaoId == null ? service.listar() : service.listarPorLicao(licaoId);
        return lista.stream().map(PerguntaDto.Response::de).toList();
    }

    @GetMapping("/{id}")
    public PerguntaDto.Response buscar(@PathVariable Long id) {
        return PerguntaDto.Response.de(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<PerguntaDto.Response> criar(@Valid @RequestBody PerguntaDto.Request req) {
        Pergunta p = new Pergunta();
        req.aplicarEm(p);
        Pergunta salva = service.salvar(p, req.licaoId());
        return Respostas.criado(salva.getId(), PerguntaDto.Response.de(salva));
    }

    @PutMapping("/{id}")
    public PerguntaDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody PerguntaDto.Request req) {
        Pergunta p = service.buscar(id);
        req.aplicarEm(p);
        return PerguntaDto.Response.de(service.salvar(p, req.licaoId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
