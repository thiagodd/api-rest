package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;
    private final ProdutoMapper mapper;

    public ProdutoController(ProdutoRepository repository, ProdutoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // Lista completa por enquanto; filtros, ordenação e paginação chegam em 13.01–13.04.
    @GetMapping
    public Lista<ProdutoResponse> listar() {
        return new Lista<>(repository.findAllByOrderByNomeAsc().stream().map(mapper::paraResponse).toList());
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody CriarProdutoRequest request) {
        var produto = repository.save(mapper.paraProduto(request));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(produto.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.paraResponse(produto));
    }

    @GetMapping("/{id}")
    public ProdutoResponse detalhar(@PathVariable UUID id) {
        return repository.findById(id)
                .map(mapper::paraResponse)
                .orElseThrow(() -> naoEncontrado(id));
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarProdutoRequest request) {
        var produto = repository.findById(id)
                .orElseThrow(() -> naoEncontrado(id));
        produto.atualizar(request.nome(), request.descricao(), Dinheiro.deCentavos(request.precoEmCentavos(), "BRL"),
                request.categoriaId(), request.atributos());
        return mapper.paraResponse(repository.save(produto));
    }

    private static RecursoNaoEncontradoException naoEncontrado(UUID id) {
        return new RecursoNaoEncontradoException("produto-nao-encontrado", id);
    }
}
