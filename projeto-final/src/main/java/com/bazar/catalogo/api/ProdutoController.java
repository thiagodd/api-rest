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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import static com.bazar.compartilhado.web.RespostasDeErro.CONFLITO;
import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;
import static com.bazar.compartilhado.web.RespostasDeErro.VALIDACAO;

@RestController
// A v1 do produto. A v2 (ProdutoV2Controller) tem o mesmo caminho: o Spring escolhe pela versão do primeiro segmento,
// e o springdoc troca o {versao} pela versão de cada controller no contrato (18.03).
@RequestMapping(path = "/v{versao}/produtos", version = "1")
@Tag(name = "produtos", description = "O catálogo de produtos")
public class ProdutoController {

    static final String ORDENACAO_NO_CONTRATO = "`campo,direcao`, repetível. Campos: `nome`, `precoEmCentavos`, "
            + "`sku` e `criadoEm`. Sem `sort`, a ordem é pelo nome (13.02).";

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
    @Operation(operationId = "listarProdutos", summary = "Lista produtos, paginando por cursor",
            description = "Os filtros se combinam com E; os valores de um mesmo filtro, com OU (13.01).")
    // O springdoc descreve o Sort por conta própria e ignora um @Parameter no argumento: a descrição vai no método.
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = ORDENACAO_NO_CONTRATO,
            array = @ArraySchema(schema = @Schema(type = "string", example = "precoEmCentavos,desc")))
    @ApiResponse(responseCode = "200", description = "Uma página de produtos", headers = @Header(name = "Link",
            description = "Os links `first`, `prev` e `next` (RFC 8288). Siga-os sem montar a URL.",
            schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ResponseEntity<Lista<ProdutoResumoResponse>> listar(@Valid @ModelAttribute FiltroProduto filtro,
            Sort sort,
            @Valid @ModelAttribute JanelaPedida janela,
            @Parameter(description = "Os campos de cada item, separados por vírgula (13.05).",
                    example = "nome,precoEmCentavos")
            @RequestParam(required = false) @Nullable String fields) {
        CamposDaResposta.selecionar(fields, ProdutoResumoResponse.class);
        return janela(filtro.paraDominio(), sort, janela);
    }

    // Uma consulta com corpo: não altera nada, mas usa POST porque os critérios não cabem bem numa URL (13.01).
    // A ordenação e a paginação continuam na query; o cliente repete o corpo para seguir os links.
    @PostMapping("/buscas")
    @Operation(operationId = "buscarProdutos", summary = "Busca produtos com os critérios no corpo",
            description = "Não altera nada. Para seguir os links do header `Link`, repita o corpo.")
    @Parameter(name = "sort", in = ParameterIn.QUERY, description = ORDENACAO_NO_CONTRATO,
            array = @ArraySchema(schema = @Schema(type = "string", example = "precoEmCentavos,desc")))
    @ApiResponse(responseCode = "200", description = "Uma página de produtos", headers = @Header(name = "Link",
            description = "Os links `first`, `prev` e `next` (RFC 8288).", schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ResponseEntity<Lista<ProdutoResumoResponse>> buscar(@Valid @RequestBody BuscaDeProdutos busca,
            Sort sort,
            @Valid @ModelAttribute JanelaPedida janela,
            @Parameter(description = "Os campos de cada item, separados por vírgula.")
            @RequestParam(required = false) @Nullable String fields) {
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
    @Operation(operationId = "criarProduto", summary = "Cadastra um produto",
            description = "O produto nasce em `RASCUNHO`. Campos somente leitura enviados são ignorados. Um SKU "
                    + "repetido dá `409` (14.01).")
    @ApiResponse(responseCode = "201", description = "O produto foi criado. Sem corpo com `Prefer: return=minimal`.",
            headers = {
                @Header(name = "Location", description = "A URI do produto", schema = @Schema(type = "string")),
                @Header(name = "Preference-Applied", description = "`return=minimal`, quando foi atendido",
                        schema = @Schema(type = "string"))})
    @ApiResponse(responseCode = "409", ref = CONFLITO)
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody CriarProdutoRequest request,
            @Parameter(description = "`return=minimal` responde sem corpo.", example = "return=minimal")
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
    @Operation(operationId = "detalharProduto", summary = "Detalha um produto")
    @ApiResponse(responseCode = "200", description = "O produto")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public ProdutoResponse detalhar(@PathVariable UUID id,
            @Parameter(description = "`categoria` traz a categoria dentro do produto.", example = "categoria")
            @RequestParam(required = false) @Nullable String embed) {
        var incorporar = ValoresDeParametro.ler("embed", embed, INCORPORAVEIS);
        var produto = servico.detalhar(id);
        var resposta = mapper.paraResponse(produto);
        return incorporar.contains("categoria")
                ? resposta.comCategoria(CategoriaResponse.de(servico.categoriaDe(produto)))
                : resposta;
    }

    // PUT substitui a parte editável inteira: um campo ausente vira null ou vazio (14.02).
    @PutMapping("/{id}")
    @Operation(operationId = "substituirProduto", summary = "Substitui a parte editável de um produto",
            description = "Um campo ausente vira `null` ou vazio. Para mudar só alguns campos, use o `PATCH`.")
    @ApiResponse(responseCode = "200", description = "O produto atualizado")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ProdutoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarProdutoRequest request) {
        var produto = servico.atualizar(id, request.nome(), request.descricao(),
                Dinheiro.deCentavos(request.precoEmCentavos(), "BRL"), request.categoriaId(), request.atributos());
        return mapper.paraResponse(produto);
    }

    // DELETE é um soft delete: o produto fica DESCONTINUADO, e um GET continua encontrando-o. Repetir dá o mesmo
    // 204 (14.03).
    @DeleteMapping("/{id}")
    @Operation(operationId = "removerProduto", summary = "Descontinua um produto",
            description = "O produto fica `DESCONTINUADO` e continua acessível pelo id. Repetir dá o mesmo `204`.")
    @ApiResponse(responseCode = "204", description = "O produto foi descontinuado")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        servico.descontinuar(id);
        return ResponseEntity.noContent().build();
    }

    // JSON Merge Patch (RFC 7396): só os campos enviados mudam, e null remove (14.02). O resultado passa pelas
    // mesmas validações e pelo mesmo serviço que o PUT.
    @PatchMapping(path = "/{id}", consumes = MERGE_PATCH)
    @Operation(operationId = "corrigirProduto", summary = "Muda só alguns campos de um produto",
            description = "JSON Merge Patch (RFC 7396) ou JSON Patch (RFC 6902). O resultado passa pelas mesmas "
                    + "validações do `PUT` (14.02). Um `test` do JSON Patch que falha dá `409`.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
                @Content(mediaType = MERGE_PATCH, schema = @Schema(type = "object",
                        description = "Os campos a mudar; `null` remove o valor.")),
                @Content(mediaType = JSON_PATCH, array = @ArraySchema(schema = @Schema(type = "object",
                        description = "Uma operação: `op`, `path` e, conforme a operação, `value` ou `from`.")))}))
    @ApiResponse(responseCode = "200", description = "O produto atualizado")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = CONFLITO)
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ProdutoResponse corrigir(@PathVariable UUID id, @RequestBody JsonNode patch) {
        var corrigido = patches.mergePatch(AtualizarProdutoRequest.de(servico.detalhar(id)), patch,
                AtualizarProdutoRequest.class);
        return atualizar(id, corrigido);
    }

    // JSON Patch (RFC 6902): uma lista de operações, aplicadas todas ou nenhuma. Um test que falha é 409.
    // Mesmo caminho e método do merge patch: no contrato, as duas são uma operação com dois media types.
    @PatchMapping(path = "/{id}", consumes = JSON_PATCH)
    @Operation(operationId = "corrigirProduto")
    public ProdutoResponse aplicarOperacoes(@PathVariable UUID id, @RequestBody JsonNode operacoes) {
        var corrigido = patches.jsonPatch(AtualizarProdutoRequest.de(servico.detalhar(id)), operacoes,
                AtualizarProdutoRequest.class);
        return atualizar(id, corrigido);
    }
}
