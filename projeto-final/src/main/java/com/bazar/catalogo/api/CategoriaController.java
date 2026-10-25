package com.bazar.catalogo.api;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.CategoriaRepository;
import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.RegraDeNegocioException;
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
    private final ProdutoRepository produtos;

    public CategoriaController(CategoriaRepository repository, ProdutoRepository produtos) {
        this.repository = repository;
        this.produtos = produtos;
    }

    @GetMapping
    public Lista<CategoriaResponse> listar(
            @RequestParam(required = false) @Nullable @Size(min = 2, max = 60) String q,
            @RequestParam(defaultValue = "false") boolean apenasRaizes) {
        var categorias = repository.findAll().stream()
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
        if (!repository.existsById(id)) {
            throw naoEncontrada(id);
        }
        return CategoriaResponse.de(repository.save(request.paraCategoria(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        var categoria = repository.findById(id).orElseThrow(() -> naoEncontrada(id));
        // Regra de negócio no controller por enquanto; ela vai para um serviço em 10.03.
        var quantidade = produtos.countByCategoriaId(id);
        if (quantidade > 0) {
            throw new RegraDeNegocioException("categoria-com-produtos",
                    Map.of("quantidadeDeProdutos", quantidade), categoria.nome(), quantidade);
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private static RecursoNaoEncontradoException naoEncontrada(UUID id) {
        return new RecursoNaoEncontradoException("categoria-nao-encontrada", id);
    }
}
