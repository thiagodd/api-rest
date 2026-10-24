package com.bazar.catalogo.api;

import java.util.Locale;
import java.util.UUID;

import com.bazar.catalogo.dominio.CategoriaRepository;
import com.bazar.compartilhado.Lista;
import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Lista<CategoriaResponse> listar(
            @RequestParam(required = false) @Nullable String q,
            @RequestParam(defaultValue = "false") boolean apenasRaizes) {
        var categorias = repository.findAll().stream()
                .filter(c -> q == null || c.nome().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))
                .filter(c -> !apenasRaizes || c.categoriaPaiId() == null)
                .map(CategoriaResponse::de)
                .toList();
        return new Lista<>(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> detalhar(@PathVariable UUID id) {
        return ResponseEntity.of(repository.findById(id).map(CategoriaResponse::de));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@RequestBody CategoriaRequest request) {
        // Ainda é UUID v4; o Bazar passa a gerar UUID v7 em 15.03.
        var categoria = repository.save(request.paraCategoria(UUID.randomUUID()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoria.id())
                .toUri();
        return ResponseEntity.created(location).body(CategoriaResponse.de(categoria));
    }

    @PutMapping("/{id}")
    public CategoriaResponse substituir(@PathVariable UUID id, @RequestBody CategoriaRequest request) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada");
        }
        return CategoriaResponse.de(repository.save(request.paraCategoria(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
