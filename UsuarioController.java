package br.com.granacerta.controller;

import br.com.granacerta.dto.PainelDto;
import br.com.granacerta.dto.UsuarioDto;
import br.com.granacerta.model.Usuario;
import br.com.granacerta.service.PainelService;
import br.com.granacerta.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final PainelService painelService;

    public UsuarioController(UsuarioService service, PainelService painelService) {
        this.service = service;
        this.painelService = painelService;
    }

    @GetMapping
    public List<UsuarioDto.Response> listar() {
        return service.listar().stream().map(UsuarioDto.Response::de).toList();
    }

    @GetMapping("/{id}")
    public UsuarioDto.Response buscar(@PathVariable Long id) {
        return UsuarioDto.Response.de(service.buscar(id));
    }

    /** Painel financeiro do mês (padrão: mês atual). Exemplo: ?mes=2026-11 */
    @GetMapping("/{id}/painel")
    public PainelDto.Response painel(@PathVariable Long id,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return PainelDto.Response.de(painelService.montar(id, mes != null ? mes : YearMonth.now()));
    }

    @PostMapping
    public ResponseEntity<UsuarioDto.Response> criar(@Valid @RequestBody UsuarioDto.Request req) {
        verificarEmail(req.email(), null);
        Usuario u = new Usuario();
        req.aplicarEm(u);
        Usuario salvo = service.salvar(u);
        return Respostas.criado(salvo.getId(), UsuarioDto.Response.de(salvo));
    }

    @PutMapping("/{id}")
    public UsuarioDto.Response atualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDto.Request req) {
        Usuario u = service.buscar(id);
        verificarEmail(req.email(), id);
        req.aplicarEm(u);
        return UsuarioDto.Response.de(service.salvar(u));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    private void verificarEmail(String email, Long idIgnorado) {
        if (service.emailEmUso(email, idIgnorado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe alguém com esse e-mail");
        }
    }
}
