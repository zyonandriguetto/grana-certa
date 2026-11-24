package br.com.granacerta.controller;

import br.com.granacerta.dto.MetaDto;
import br.com.granacerta.model.Meta;
import br.com.granacerta.service.MetaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaService service;

    public MetaController(MetaService service) {
        this.service = service;
    }

    /** Filtro opcional: ?usuarioId= */
    @GetMapping
    public List<MetaDto.Response> listar(@RequestParam(required = false) Long usuarioId) {
        List<Meta> lista = usuarioId == null ? service.listar() : service.listarPorUsuario(usuarioId);
        return lista.stream().map(MetaDto.Response::de).toList();
    }

    /** Detalhe da meta, incluindo os depósitos (aportes). */
    @GetMapping("/{id}")
    public MetaDto.Response buscar(@PathVariable Long id) {
        return MetaDto.Response.de(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<MetaDto.Response> criar(@Valid @RequestBody MetaDto.Request req) {
        Meta m = new Meta();
        req.aplicarEm(m);
        Meta salva = service.salvar(m, req.usuarioId());
        return Respostas.criado(salva.getId(), MetaDto.Response.de(salva));
    }

    @PutMapping("/{id}")
    public MetaDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody MetaDto.Request req) {
        Meta m = service.buscar(id);
        req.aplicarEm(m);
        return MetaDto.Response.de(service.salvar(m, req.usuarioId()));
    }

    /** Os depósitos continuam no histórico de transações, só perdem o vínculo com a meta. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
