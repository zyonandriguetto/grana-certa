package br.com.granacerta.controller;

import br.com.granacerta.dto.LicaoDto;
import br.com.granacerta.model.Licao;
import br.com.granacerta.service.LicaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/licoes")
public class LicaoController {

    private final LicaoService service;

    public LicaoController(LicaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<LicaoDto.Response> listar() {
        return service.listar().stream().map(LicaoDto.Response::de).toList();
    }

    @GetMapping("/{id}")
    public LicaoDto.Response buscar(@PathVariable Long id) {
        return LicaoDto.Response.de(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<LicaoDto.Response> criar(@Valid @RequestBody LicaoDto.Request req) {
        Licao l = new Licao();
        req.aplicarEm(l);
        // sem "ordem" informada, a lição vai para o fim da trilha
        if (req.ordem() == null) l.setOrdem((int) service.contar() + 1);
        Licao salva = service.salvar(l);
        return Respostas.criado(salva.getId(), LicaoDto.Response.de(salva));
    }

    @PutMapping("/{id}")
    public LicaoDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody LicaoDto.Request req) {
        Licao l = service.buscar(id);
        req.aplicarEm(l);
        return LicaoDto.Response.de(service.salvar(l));
    }

    /** Exclui também as perguntas e tentativas da lição. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
