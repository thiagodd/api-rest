package com.bazar.catalogo.api;

import java.util.Locale;
import java.util.UUID;

import com.bazar.catalogo.dominio.CategoriaRepository;
import com.bazar.catalogo.dominio.CategoriaService;
import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.web.Auditavel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/categorias")
public class CategoriaController {

    private final CategoriaRepository repository;
    private final CategoriaService servico;

    public CategoriaController(CategoriaRepository repository, CategoriaService servico) {
        this.repository = repository;
        this.servico = servico;
    }

    @GetMapping
    public Lista<CategoriaResponse> listar(
            @RequestParam(required = false) @Nullable @Size(min = 2, max = 60) String q,
            @RequestParam(defaultValue = "false") boolean apenasRaizes) {
        var categorias = repository.findAllByOrderByNomeAsc().stream()
                .filter(c -> q == null || c.nome().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))
                .filter(c -> !apenasRaizes || c.categoriaPaiId() == null)
                .map(CategoriaResponse::de)
                .toList();
        return new Lista<>(categorias);
    }

    @GetMapping("/{id}")
    public CategoriaResponse detalhar(@PathVariable UUID id) {
        return repository.findById(id)
                .map(CategoriaResponse::de)
                .orElseThrow(() -> naoEncontrada(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        // Ainda é UUID v4; o Bazar passa a gerar UUID v7 em 15.03.
        var categoria = repository.save(request.paraCategoria(UUID.randomUUID()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoria.id())
                .toUri();
        return ResponseEntity.created(location).body(CategoriaResponse.de(categoria));
    }

    @PutMapping("/{id}")
    public CategoriaResponse substituir(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(servico.substituir(id, request.nome(), request.slug(), request.categoriaPaiId()));
    }

    @Auditavel("categoria.remover")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        servico.remover(id);
        return ResponseEntity.noContent().build();
    }

    private static RecursoNaoEncontradoException naoEncontrada(UUID id) {
        return new RecursoNaoEncontradoException("categoria-nao-encontrada", id);
    }
}
