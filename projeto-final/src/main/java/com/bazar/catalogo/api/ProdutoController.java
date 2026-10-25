package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.FiltroDeProdutos;
import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.catalogo.dominio.ProdutoService;
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
    private final ProdutoService servico;
    private final ProdutoMapper mapper;

    public ProdutoController(ProdutoRepository repository, ProdutoService servico, ProdutoMapper mapper) {
        this.repository = repository;
        this.servico = servico;
        this.mapper = mapper;
    }

    // Uma projeção, e não entidades: só as colunas da listagem (10.05).
    // Os filtros já existem no serviço; os parâmetros HTTP, a ordenação e a paginação chegam em 13.01–13.04.
    @GetMapping
    public Lista<ProdutoResumoResponse> listar() {
        return new Lista<>(servico.listar(FiltroDeProdutos.nenhum()).stream().map(mapper::paraResumo).toList());
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody CriarProdutoRequest request) {
        var produto = servico.criar(mapper.paraProduto(request));
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
        var produto = servico.atualizar(id, request.nome(), request.descricao(),
                Dinheiro.deCentavos(request.precoEmCentavos(), "BRL"), request.categoriaId(), request.atributos());
        return mapper.paraResponse(produto);
    }

    private static RecursoNaoEncontradoException naoEncontrado(UUID id) {
        return new RecursoNaoEncontradoException("produto-nao-encontrado", id);
    }
}
