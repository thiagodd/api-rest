package com.bazar.catalogo.api;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import com.bazar.catalogo.dominio.FiltroDeProdutos;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.web.AplicadorDePatch;
import com.bazar.compartilhado.web.CamposDaResposta;
import com.bazar.compartilhado.web.CamposOrdenaveis;
import com.bazar.compartilhado.web.JanelaPedida;
import com.bazar.compartilhado.web.LinksDeCursor;
import com.bazar.compartilhado.web.PreferenciaDeRetorno;
import com.bazar.compartilhado.web.ValoresDeParametro;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    static final String MERGE_PATCH = "application/merge-patch+json";
    static final String JSON_PATCH = "application/json-patch+json";

    private static final SortedSet<String> INCORPORAVEIS = new TreeSet<>(Set.of("categoria"));

    // O controller só conhece o serviço, o mapper e o aplicador de patches: no @WebMvcTest, basta substituir o
    // serviço (11.03).
    private final ProdutoService servico;
    private final ProdutoMapper mapper;
    private final AplicadorDePatch patches;

    public ProdutoController(ProdutoService servico, ProdutoMapper mapper, AplicadorDePatch patches) {
        this.servico = servico;
        this.mapper = mapper;
        this.patches = patches;
    }

    // Uma projeção, e não entidades: só as colunas da listagem (10.05).
    // Os filtros chegam como parâmetros de query (13.01), a ordenação no sort (13.02) e a paginação por cursor em
    // limit e cursor (13.04), com first, prev e next no header Link.
    @GetMapping
    public ResponseEntity<Lista<ProdutoResumoResponse>> listar(@Valid @ModelAttribute FiltroProduto filtro, Sort sort,
            @Valid @ModelAttribute JanelaPedida janela, @RequestParam(required = false) @Nullable String fields) {
        CamposDaResposta.selecionar(fields, ProdutoResumoResponse.class);
        return janela(filtro.paraDominio(), sort, janela);
    }

    // Uma consulta com corpo: não altera nada, mas usa POST porque os critérios não cabem bem numa URL (13.01).
    // A ordenação e a paginação continuam na query; o cliente repete o corpo para seguir os links.
    @PostMapping("/buscas")
    public ResponseEntity<Lista<ProdutoResumoResponse>> buscar(@Valid @RequestBody BuscaDeProdutos busca, Sort sort,
            @Valid @ModelAttribute JanelaPedida janela, @RequestParam(required = false) @Nullable String fields) {
        CamposDaResposta.selecionar(fields, ProdutoResumoResponse.class);
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

    // 201 com Location e a representação; com Prefer: return=minimal, 201 sem corpo (14.01).
    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody CriarProdutoRequest request,
            @RequestHeader(name = "Prefer", required = false) @Nullable String prefer) {
        var produto = servico.criar(mapper.paraProduto(request));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(produto.getId())
                .toUri();
        if (PreferenciaDeRetorno.minima(prefer)) {
            return ResponseEntity.created(location)
                    .header(PreferenciaDeRetorno.APLICADA, "return=minimal")
                    .build();
        }
        return ResponseEntity.created(location).body(mapper.paraResponse(produto));
    }

    // ?embed=categoria traz a categoria dentro do produto, em vez de só o categoriaId (13.05).
    @GetMapping("/{id}")
    public ProdutoResponse detalhar(@PathVariable UUID id, @RequestParam(required = false) @Nullable String embed) {
        var incorporar = ValoresDeParametro.ler("embed", embed, INCORPORAVEIS);
        var produto = servico.detalhar(id);
        var resposta = mapper.paraResponse(produto);
        return incorporar.contains("categoria")
                ? resposta.comCategoria(CategoriaResponse.de(servico.categoriaDe(produto)))
                : resposta;
    }

    // PUT substitui a parte editável inteira: um campo ausente vira null ou vazio (14.02).
    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarProdutoRequest request) {
        var produto = servico.atualizar(id, request.nome(), request.descricao(),
                Dinheiro.deCentavos(request.precoEmCentavos(), "BRL"), request.categoriaId(), request.atributos());
        return mapper.paraResponse(produto);
    }

    // JSON Merge Patch (RFC 7396): só os campos enviados mudam, e null remove (14.02). O resultado passa pelas
    // mesmas validações e pelo mesmo serviço que o PUT.
    @PatchMapping(path = "/{id}", consumes = MERGE_PATCH)
    public ProdutoResponse corrigir(@PathVariable UUID id, @RequestBody JsonNode patch) {
        var corrigido = patches.mergePatch(AtualizarProdutoRequest.de(servico.detalhar(id)), patch,
                AtualizarProdutoRequest.class);
        return atualizar(id, corrigido);
    }

    // JSON Patch (RFC 6902): uma lista de operações, aplicadas todas ou nenhuma. Um test que falha é 409.
    @PatchMapping(path = "/{id}", consumes = JSON_PATCH)
    public ProdutoResponse aplicarOperacoes(@PathVariable UUID id, @RequestBody JsonNode operacoes) {
        var corrigido = patches.jsonPatch(AtualizarProdutoRequest.de(servico.detalhar(id)), operacoes,
                AtualizarProdutoRequest.class);
        return atualizar(id, corrigido);
    }
}
