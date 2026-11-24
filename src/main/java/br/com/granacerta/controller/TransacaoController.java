package br.com.granacerta.controller;

import br.com.granacerta.dto.TransacaoDto;
import br.com.granacerta.model.Transacao;
import br.com.granacerta.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    /** Filtro opcional: ?usuarioId= */
    @GetMapping
    public List<TransacaoDto.Response> listar(@RequestParam(required = false) Long usuarioId) {
        List<Transacao> lista = usuarioId == null ? service.listar() : service.listarPorUsuario(usuarioId);
        return lista.stream().map(TransacaoDto.Response::de).toList();
    }

    @GetMapping("/{id}")
    public TransacaoDto.Response buscar(@PathVariable Long id) {
        return TransacaoDto.Response.de(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<TransacaoDto.Response> criar(@Valid @RequestBody TransacaoDto.Request req) {
        Transacao t = new Transacao();
        req.aplicarEm(t);
        Transacao salva = service.salvar(t, req.usuarioId(), req.categoriaId(), req.metaId());
        return Respostas.criado(salva.getId(), TransacaoDto.Response.de(salva));
    }

    @PutMapping("/{id}")
    public TransacaoDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody TransacaoDto.Request req) {
        Transacao t = service.buscar(id);
        req.aplicarEm(t);
        return TransacaoDto.Response.de(service.salvar(t, req.usuarioId(), req.categoriaId(), req.metaId()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
