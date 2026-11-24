package br.com.granacerta.controller;

import br.com.granacerta.dto.TentativaDto;
import br.com.granacerta.model.Tentativa;
import br.com.granacerta.service.RegraNegocioException;
import br.com.granacerta.service.TentativaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD administrativo das tentativas (o fluxo normal é responder o quiz em /api/quiz). */
@RestController
@RequestMapping("/api/tentativas")
public class TentativaController {

    private final TentativaService service;

    public TentativaController(TentativaService service) {
        this.service = service;
    }

    @GetMapping
    public List<TentativaDto.Response> listar() {
        return service.listar().stream().map(TentativaDto.Response::de).toList();
    }

    @GetMapping("/{id}")
    public TentativaDto.Response buscar(@PathVariable Long id) {
        return TentativaDto.Response.de(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<TentativaDto.Response> criar(@Valid @RequestBody TentativaDto.Request req) {
        validarAcertos(req);
        Tentativa t = new Tentativa();
        req.aplicarEm(t);
        Tentativa salva = service.salvar(t, req.usuarioId(), req.licaoId());
        return Respostas.criado(salva.getId(), TentativaDto.Response.de(salva));
    }

    @PutMapping("/{id}")
    public TentativaDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody TentativaDto.Request req) {
        validarAcertos(req);
        Tentativa t = service.buscar(id);
        req.aplicarEm(t);
        return TentativaDto.Response.de(service.salvar(t, req.usuarioId(), req.licaoId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    private static void validarAcertos(TentativaDto.Request req) {
        if (req.acertos() > req.totalPerguntas()) {
            throw new RegraNegocioException("Acertos não pode passar do total de perguntas");
        }
    }
}
