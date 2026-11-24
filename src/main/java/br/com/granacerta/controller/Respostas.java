package br.com.granacerta.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** Atalhos para montar respostas HTTP padronizadas. */
final class Respostas {

    private Respostas() {}

    /** 201 Created com o cabeçalho Location apontando para {@code <caminho atual>/{id}}. */
    static <T> ResponseEntity<T> criado(Long id, T corpo) {
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(local).body(corpo);
    }
}
