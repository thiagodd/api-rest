package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.compartilhado.Dinheiro;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/produtos")
public class ProdutoController {

    private final ProdutoRepository repository;

    public ProdutoController(ProdutoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@RequestBody CriarProdutoRequest request) {
        var produto = repository.save(request.paraProduto());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(produto.getId())
                .toUri();
        return ResponseEntity.created(location).body(ProdutoResponse.de(produto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> detalhar(@PathVariable UUID id) {
        return ResponseEntity.of(repository.findById(id).map(ProdutoResponse::de));
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable UUID id, @RequestBody AtualizarProdutoRequest request) {
        var produto = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        produto.atualizar(request.nome(), request.descricao(), Dinheiro.deCentavos(request.precoEmCentavos(), "BRL"),
                request.categoriaId(), request.atributos());
        return ProdutoResponse.de(repository.save(produto));
    }
}
