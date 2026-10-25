package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.Lista;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    // O controller só conhece o serviço e o mapper: no @WebMvcTest, basta substituir o serviço (11.03).
    private final ProdutoService servico;
    private final ProdutoMapper mapper;

    public ProdutoController(ProdutoService servico, ProdutoMapper mapper) {
        this.servico = servico;
        this.mapper = mapper;
    }

    // Uma projeção, e não entidades: só as colunas da listagem (10.05).
    // Os filtros chegam como parâmetros de query (13.01); a ordenação e a paginação, em 13.02–13.04.
    @GetMapping
    public Lista<ProdutoResumoResponse> listar(@Valid @ModelAttribute FiltroProduto filtro) {
        return new Lista<>(servico.listar(filtro.paraDominio()).stream().map(mapper::paraResumo).toList());
    }

    // Uma consulta com corpo: não altera nada, mas usa POST porque os critérios não cabem bem numa URL (13.01).
    @PostMapping("/buscas")
    public Lista<ProdutoResumoResponse> buscar(@Valid @RequestBody BuscaDeProdutos busca) {
        return new Lista<>(servico.listar(busca.paraDominio()).stream().map(mapper::paraResumo).toList());
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
        return mapper.paraResponse(servico.detalhar(id));
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarProdutoRequest request) {
        var produto = servico.atualizar(id, request.nome(), request.descricao(),
                Dinheiro.deCentavos(request.precoEmCentavos(), "BRL"), request.categoriaId(), request.atributos());
        return mapper.paraResponse(produto);
    }
}
