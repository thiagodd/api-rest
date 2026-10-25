package com.bazar.catalogo.api;

import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.FiltroDeProdutos;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.web.CamposOrdenaveis;
import com.bazar.compartilhado.web.JanelaPedida;
import com.bazar.compartilhado.web.LinksDeCursor;
import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
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

    // Os campos de ?sort=: o nome no contrato e a propriedade da entidade (13.02). Sem sort, ordena pelo nome.
    static final CamposOrdenaveis ORDENACAO = new CamposOrdenaveis(Map.of(
            "nome", "nome",
            "precoEmCentavos", "preco.valor",
            "sku", "sku",
            "criadoEm", "criadoEm"), Sort.by("nome"));

    // O controller só conhece o serviço e o mapper: no @WebMvcTest, basta substituir o serviço (11.03).
    private final ProdutoService servico;
    private final ProdutoMapper mapper;

    public ProdutoController(ProdutoService servico, ProdutoMapper mapper) {
        this.servico = servico;
        this.mapper = mapper;
    }

    // Uma projeção, e não entidades: só as colunas da listagem (10.05).
    // Os filtros chegam como parâmetros de query (13.01), a ordenação no sort (13.02) e a paginação por cursor em
    // limit e cursor (13.04), com first, prev e next no header Link.
    @GetMapping
    public ResponseEntity<Lista<ProdutoResumoResponse>> listar(@Valid @ModelAttribute FiltroProduto filtro, Sort sort,
            @Valid @ModelAttribute JanelaPedida janela) {
        return janela(filtro.paraDominio(), sort, janela);
    }

    // Uma consulta com corpo: não altera nada, mas usa POST porque os critérios não cabem bem numa URL (13.01).
    // A ordenação e a paginação continuam na query; o cliente repete o corpo para seguir os links.
    @PostMapping("/buscas")
    public ResponseEntity<Lista<ProdutoResumoResponse>> buscar(@Valid @RequestBody BuscaDeProdutos busca, Sort sort,
            @Valid @ModelAttribute JanelaPedida janela) {
        return janela(busca.paraDominio(), sort, janela);
    }

    private ResponseEntity<Lista<ProdutoResumoResponse>> janela(FiltroDeProdutos filtro, Sort sort,
            JanelaPedida janela) {
        var ordenacao = ORDENACAO.traduzir(sort);
        var posicao = CursorDeProdutos.posicao(janela.cursor(), filtro, ordenacao);
        var produtos = servico.listar(filtro, ordenacao, posicao, janela.tamanho());
        return ResponseEntity.ok()
                .headers(LinksDeCursor.de(produtos, posicao,
                        (chaves, paraTras) -> CursorDeProdutos.de(filtro, ordenacao, chaves, paraTras)))
                .body(new Lista<>(produtos.stream().map(mapper::paraResumo).toList()));
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
