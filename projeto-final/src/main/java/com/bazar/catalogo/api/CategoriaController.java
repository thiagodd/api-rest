package com.bazar.catalogo.api;

import java.util.Locale;
import java.util.UUID;

import com.bazar.catalogo.dominio.Categoria;
import com.bazar.catalogo.dominio.CategoriaRepository;
import com.bazar.compartilhado.Lista;
import org.jspecify.annotations.Nullable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;

    public CategoriaController(CategoriaRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Lista<Categoria> listar(
            @RequestParam(required = false) @Nullable String q,
            @RequestParam(defaultValue = "false") boolean apenasRaizes) {
        var categorias = repository.findAll().stream()
                .filter(c -> q == null || c.nome().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))
                .filter(c -> !apenasRaizes || c.categoriaPaiId() == null)
                .toList();
        return new Lista<>(categorias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> detalhar(@PathVariable UUID id) {
        return ResponseEntity.of(repository.findById(id));
    }
}
