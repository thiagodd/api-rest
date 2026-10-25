package com.bazar.catalogo.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.bazar.catalogo.dominio.Categoria;
import com.bazar.catalogo.dominio.FiltroDeProdutos;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoResumo;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.catalogo.dominio.StatusProduto;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.DinheiroMapperImpl;
import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.compartilhado.web.AplicadorDePatch;
import com.bazar.compartilhado.web.ListaCsvHttpMessageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.ELETRONICOS;
import static com.bazar.catalogo.dominio.ProdutoDeTeste.PROGRAMACAO;
import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Um slice test: só a camada web sobe (controller, Jackson, validação, TratadorGlobalDeErros, filtros).
// O serviço é um dublê; nada aqui toca no banco. O que depende do PostgreSQL está em ProdutoIntegracaoTest (11.04).
@WebMvcTest(ProdutoController.class)
@AutoConfigureRestTestClient
@Import({ ProdutoMapperImpl.class, DinheiroMapperImpl.class, AplicadorDePatch.class })
class ProdutoControllerTest {

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ProdutoService produtos;

    @Test
    void criaUmProdutoComoRascunho() {
        when(produtos.criar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "descricao": "Um guia de boas práticas.",
                          "precoEmCentavos": 12990, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                          "atributos": { "autor": "Robert C. Martin" } }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                  "status": "RASCUNHO", "atributos": { "autor": "Robert C. Martin" } }
                """);
        assertThat(resposta).bodyJson().doesNotHavePath("$.versao");
        var criado = ArgumentCaptor.forClass(Produto.class);
        verify(produtos).criar(criado.capture());
        assertThat(resposta).hasHeader(HttpHeaders.LOCATION, "http://localhost/v1/produtos/" + criado.getValue().getId());
    }

    @Test
    void ignoraOsCamposQueOClienteNaoControla() {
        when(produtos.criar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "id": "0192f39f-2222-7000-8000-000000000042", "sku": "LIV-0043", "nome": "Refactoring",
                          "precoEmCentavos": 15900, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                          "status": "ATIVO", "versao": 99, "criadoEm": "2020-01-01T00:00:00Z" }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        assertThat(resposta).bodyJson().extractingPath("$.status").isEqualTo("RASCUNHO");
        assertThat(resposta).bodyJson().extractingPath("$.id").isNotEqualTo("0192f39f-2222-7000-8000-000000000042");
        assertThat(resposta).bodyJson().extractingPath("$.criadoEm").asString().doesNotStartWith("2020");
        assertThat(resposta).bodyJson().extractingPath("$.atributos").isEqualTo(Map.of());
    }

    @Test
    void repassaAoServicoOsCamposDaAtualizacao() {
        var ddd = umProduto().comSku("LIV-0044").comNome("Domain-Driven Design").comPreco(17990).build();
        when(produtos.atualizar(eq(ddd.getId()), eq("Domain-Driven Design"), isNull(),
                eq(Dinheiro.deCentavos(17990, "BRL")), eq(PROGRAMACAO), eq(Map.of("autor", "Eric Evans"))))
                .thenReturn(ddd);

        assertThat(mvc.put().uri("/v1/produtos/{id}", ddd.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Domain-Driven Design", "descricao": null, "precoEmCentavos": 17990,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003", "atributos": { "autor": "Eric Evans" } }
                        """))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        { "sku": "LIV-0044", "nome": "Domain-Driven Design", "precoEmCentavos": 17990 }
                        """);
    }

    @Test
    void rejeitaUmProdutoInvalidoSemChamarOServico() {
        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "", "nome": "Clean Code", "precoEmCentavos": -5,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getMvcResult().getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
        var erros = ((MethodArgumentNotValidException) resposta.getMvcResult().getResolvedException()).getFieldErrors();
        assertThat(erros).extracting(FieldError::getField).containsOnly("sku", "precoEmCentavos");
        verifyNoInteractions(produtos);
    }

    @Test
    void rejeitaUmProdutoSemPreco() {
        assertThat(mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void listaEmJsonPorPadraoEEmCsvQuandoPedido() {
        var id = UUID.fromString("0192f39f-2222-7000-8000-000000000042");
        when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(new ProdutoResumo(id,
                "LIV-0042", "Clean Code, 2ª edição", Dinheiro.deCentavos(12990, "BRL"), StatusProduto.RASCUNHO,
                PROGRAMACAO, Instant.parse("2026-10-20T10:00:00Z"))), i -> ScrollPosition.keyset()));

        assertThat(mvc.get().uri("/v1/produtos"))
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().extractingPath("$.itens[0].precoEmCentavos").isEqualTo(12990);

        var csv = mvc.get().uri("/v1/produtos").accept(ListaCsvHttpMessageConverter.TEXT_CSV).exchange();
        assertThat(csv).hasStatusOk().hasContentType(ListaCsvHttpMessageConverter.TEXT_CSV);
        assertThat(csv).bodyText().isEqualTo("""
                id,sku,nome,precoEmCentavos,categoriaId,status\r
                0192f39f-2222-7000-8000-000000000042,LIV-0042,"Clean Code, 2ª edição",12990,\
                0192f3a0-5555-7000-8000-000000000003,RASCUNHO\r
                """);
    }

    @Test
    void responde406QuandoPedeCsvDeUmItem() {
        var cleanCode = umProduto().build();
        when(produtos.detalhar(cleanCode.getId())).thenReturn(cleanCode);

        // O CSV só existe para listas: para um item, nenhum conversor escreve text/csv.
        assertThat(mvc.get().uri("/v1/produtos/{id}", cleanCode.getId()).accept(ListaCsvHttpMessageConverter.TEXT_CSV))
                .hasStatus(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void recusaUmaCategoriaQueNaoExisteCom422() {
        when(produtos.criar(any())).thenThrow(new CampoInvalidoException("categoriaId", "bazar.produto.categoria-inexistente"));

        assertThat(mvc.post().uri("/v1/produtos").contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-BR")
                .content("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000999" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/validacao", "codigo": "VALIDACAO",
                          "erros": [ { "campo": "categoriaId", "mensagem": "A categoria informada não existe." } ] }
                        """);
    }

    @Test
    void responde404ComProblemDetails() {
        var id = UUID.fromString("0192f39f-2222-7000-8000-000000000999");
        when(produtos.detalhar(id)).thenThrow(new RecursoNaoEncontradoException(TipoProblema.PRODUTO_NAO_ENCONTRADO, id));

        assertThat(mvc.get().uri("/v1/produtos/{id}", id).header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.type", v -> assertThat(v).isEqualTo("https://api.bazar.example/problemas/produto-nao-encontrado"))
                .hasPathSatisfying("$.title", v -> assertThat(v).isEqualTo("Produto não encontrado"))
                .hasPathSatisfying("$.detail", v -> assertThat(v)
                        .isEqualTo("Não existe produto com id 0192f39f-2222-7000-8000-000000000999."))
                .hasPathSatisfying("$.instance", v -> assertThat(v).isEqualTo("/v1/produtos/0192f39f-2222-7000-8000-000000000999"))
                .hasPathSatisfying("$.codigo", v -> assertThat(v).isEqualTo("PRODUTO_NAO_ENCONTRADO"))
                .hasPathSatisfying("$.traceId", v -> assertThat(v).asString().matches("[0-9a-f]{32}"));
    }

    @Test
    void responde422ComUmErroPorCampoSemEcoarOValor() {
        assertThat(mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                .content("""
                        { "sku": "liv-42", "nome": "Clean Code", "precoEmCentavos": -5,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/validacao", "title": "Dados inválidos",
                          "status": 422, "detail": "A requisição tem 2 campos inválidos.", "codigo": "VALIDACAO",
                          "erros": [
                            { "campo": "precoEmCentavos", "mensagem": "deve ser maior que 0" },
                            { "campo": "sku", "mensagem": "deve seguir o formato AAA-0000 (ex.: LIV-0042)" } ] }
                        """);
    }

    @Test
    void responde400ParaUmIdMalformadoSemEcoarOValor() {
        assertThat(mvc.get().uri("/v1/produtos/abc").header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.detail", v -> assertThat(v).isEqualTo("O valor do parâmetro 'id' é inválido."));
    }

    // A coleção não aceita DELETE (só cada produto, a partir da 14.03).
    @Test
    void responde405NoMesmoFormato() {
        assertThat(mvc.delete().uri("/v1/produtos"))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.codigo", v -> assertThat(v).isEqualTo("METHOD_NOT_ALLOWED"))
                .hasPath("$.traceId");
    }

    @Test
    void responde400ParaUmCorpoIlegivel() {
        assertThat(mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"sku\": "))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", v -> assertThat(v).isEqualTo("REQUISICAO_INVALIDA"));
    }

    // Os mesmos contratos, escritos com o RestTestClient. Aqui ele usa o MockMvc por baixo; em 11.04 o mesmo
    // código fala com um servidor de verdade.
    @Nested
    class Filtros {

        final ArgumentCaptor<FiltroDeProdutos> filtro = ArgumentCaptor.forClass(FiltroDeProdutos.class);

        @Test
        void convertemOsParametrosNoFiltroDoDominio() {
            when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(), i -> ScrollPosition.keyset()));

            var resposta = mvc.get().uri("/v1/produtos?q=code&categoriaId={a},{b}&status=ATIVO&status=RASCUNHO"
                    + "&precoMin=1000&precoMax=20050&criadoApos=2026-01-01T00:00:00Z", PROGRAMACAO, ELETRONICOS).exchange();

            assertThat(resposta).hasStatusOk();
            verify(produtos).listar(filtro.capture(), any(), any(), anyInt());
            assertThat(filtro.getValue()).isEqualTo(new FiltroDeProdutos("code", null, Set.of(PROGRAMACAO, ELETRONICOS),
                    Set.of(StatusProduto.ATIVO, StatusProduto.RASCUNHO), new BigDecimal("10.00"), new BigDecimal("200.50"),
                    Instant.parse("2026-01-01T00:00:00Z"), Map.of()));
        }

        @Test
        void semParametrosNaoFiltraNada() {
            when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(), i -> ScrollPosition.keyset()));

            assertThat(mvc.get().uri("/v1/produtos").exchange()).hasStatusOk();

            verify(produtos).listar(eq(FiltroDeProdutos.nenhum()), any(), any(), anyInt());
        }

        @Test
        void parametroVazioEhOMesmoQueAusente() {
            when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(), i -> ScrollPosition.keyset()));

            assertThat(mvc.get().uri("/v1/produtos?q=&sku=&categoriaId=&status=&precoMin=&criadoApos=").exchange())
                    .hasStatusOk();

            verify(produtos).listar(eq(FiltroDeProdutos.nenhum()), any(), any(), anyInt());
        }

        @Test
        void misturarVirgulaERepeticaoNoMesmoParametroNaoConverte() {
            // Cada forma funciona sozinha; juntas, o Spring recebe ["RASCUNHO,ATIVO", "DESCONTINUADO"] e não divide.
            // "RASCUNHO,ATIVO" é um texto fora da lista: 422, como status=ESGOTADO (16.01).
            assertThat(mvc.get().uri("/v1/produtos?status=RASCUNHO,ATIVO&status=DESCONTINUADO").exchange())
                    .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                    .bodyJson().extractingPath("$.erros[0].campo").isEqualTo("status");
            verifyNoInteractions(produtos);
        }

        @Test
        void recusaUmParametroQueNaoEhFiltro() {
            assertThat(mvc.get().uri("/v1/produtos?categoria={id}", PROGRAMACAO).exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().isLenientlyEqualTo("""
                            { "type": "https://api.bazar.example/problemas/parametro-desconhecido",
                              "parametros": ["categoria"] }
                            """);
            verifyNoInteractions(produtos);
        }

        @Test
        void recusaUmaFaixaDePrecoInvertida() {
            var resposta = mvc.get().uri("/v1/produtos?precoMin=5000&precoMax=1000").exchange();

            assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                    .bodyJson().extractingPath("$.erros[0].campo").isEqualTo("precoMax");
            verifyNoInteractions(produtos);
        }

        // Um valor com o tipo errado (precoMin=abc) é 400, e leva junto os outros parâmetros que não converteram.
        @Test
        void recusaUmValorQueNaoConverteSemEcoarOValor() {
            var resposta = mvc.get().uri("/v1/produtos?status=ESGOTADO&precoMin=abc").exchange();

            assertThat(resposta).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                    { "type": "https://api.bazar.example/problemas/requisicao-invalida",
                      "parametros": ["precoMin", "status"] }
                    """);
            assertThat(resposta).bodyText().doesNotContain("ESGOTADO", "abc", "java.");
            verifyNoInteractions(produtos);
        }

        // Tipo certo (texto), valor fora da lista: 422, como um enum inválido no corpo (16.01).
        @Test
        void umStatusQueNaoExisteEhUmErroDeValidacaoComOsValoresAceitos() {
            var resposta = mvc.get().uri("/v1/produtos?status=ESGOTADO").exchange();

            assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
            assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                    { "type": "https://api.bazar.example/problemas/validacao",
                      "erros": [ { "campo": "status",
                                   "mensagem": "deve ser um destes valores: RASCUNHO, ATIVO, DESCONTINUADO." } ] }
                    """);
            verifyNoInteractions(produtos);
        }

        @Test
        void aBuscaRecebeOsCriteriosNoCorpo() {
            when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(), i -> ScrollPosition.keyset()));

            var resposta = mvc.post().uri("/v1/produtos/buscas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "categoriaIds": ["%s"], "precoMax": 15000,
                              "atributos": { "autor": "Robert C. Martin" } }
                            """.formatted(PROGRAMACAO))
                    .exchange();

            assertThat(resposta).hasStatusOk();
            verify(produtos).listar(filtro.capture(), any(), any(), anyInt());
            assertThat(filtro.getValue()).isEqualTo(FiltroDeProdutos.nenhum().daCategoria(PROGRAMACAO)
                    .custando(null, new BigDecimal("150.00")).comAtributo("autor", "Robert C. Martin"));
        }

        @Test
        void aBuscaRecusaUmCriterioDesconhecido() {
            // "categoriaId" no singular é o nome do parâmetro de GET; no corpo, o critério é "categoriaIds".
            var resposta = mvc.post().uri("/v1/produtos/buscas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "categoriaId": "%s" }
                            """.formatted(PROGRAMACAO))
                    .exchange();

            assertThat(resposta).hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().isLenientlyEqualTo("""
                            { "type": "https://api.bazar.example/problemas/parametro-desconhecido",
                              "parametros": ["categoriaId"] }
                            """);
            verifyNoInteractions(produtos);
        }

        @Test
        void aBuscaValidaOsNomesDosAtributos() {
            var resposta = mvc.post().uri("/v1/produtos/buscas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "atributos": { "autor'; drop table produto; --": "x" } }
                            """)
                    .exchange();

            assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
            verifyNoInteractions(produtos);
        }
    }

    @Nested
    class RespostasParciais {

        @BeforeEach
        void umProdutoNaLista() {
            when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(new ProdutoResumo(
                    UUID.fromString("0192f39f-2222-7000-8000-000000000042"), "LIV-0042", "Clean Code",
                    Dinheiro.deCentavos(12990, "BRL"), StatusProduto.ATIVO, PROGRAMACAO,
                    Instant.parse("2026-10-20T10:00:00Z"))), i -> ScrollPosition.keyset()));
        }

        @Test
        void fieldsDevolveSoOsCamposPedidosDeCadaItem() {
            assertThat(mvc.get().uri("/v1/produtos?fields=id,nome,precoEmCentavos")).hasStatusOk()
                    .bodyJson().isStrictlyEqualTo("""
                            { "itens": [ { "id": "0192f39f-2222-7000-8000-000000000042", "nome": "Clean Code",
                                           "precoEmCentavos": 12990 } ] }
                            """);
        }

        @Test
        void semFieldsAListaSaiCompleta() {
            assertThat(mvc.get().uri("/v1/produtos")).hasStatusOk()
                    .bodyJson().extractingPath("$.itens[0]").asMap()
                    .containsOnlyKeys("id", "sku", "nome", "precoEmCentavos", "categoriaId", "status");
        }

        @Test
        void recusaUmCampoQueOItemNaoTem() {
            assertThat(mvc.get().uri("/v1/produtos?fields=nome,descricao,preco"))
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().isLenientlyEqualTo("""
                            { "type": "https://api.bazar.example/problemas/valor-nao-aceito", "parametro": "fields",
                              "recusados": [ "descricao", "preco" ],
                              "aceitos": [ "categoriaId", "id", "nome", "precoEmCentavos", "sku", "status" ] }
                            """);
        }

        @Test
        void embedCategoriaTrazACategoriaDentroDoProduto() {
            var cleanCode = umProduto().build();
            when(produtos.detalhar(cleanCode.getId())).thenReturn(cleanCode);
            when(produtos.categoriaDe(cleanCode)).thenReturn(
                    new Categoria(PROGRAMACAO, "Programação", "programacao", null));

            assertThat(mvc.get().uri("/v1/produtos/{id}?embed=categoria", cleanCode.getId())).hasStatusOk()
                    .bodyJson().isLenientlyEqualTo("""
                            { "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                              "categoria": { "id": "0192f3a0-5555-7000-8000-000000000003", "nome": "Programação",
                                             "slug": "programacao" } }
                            """);
            // Sem embed, o campo nem aparece.
            assertThat(mvc.get().uri("/v1/produtos/{id}", cleanCode.getId()))
                    .bodyJson().doesNotHavePath("$.categoria");
        }

        @Test
        void recusaUmEmbedQueOProdutoNaoOferece() {
            assertThat(mvc.get().uri("/v1/produtos/{id}?embed=estoque", UUID.randomUUID()))
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().extractingPath("$.aceitos").asArray().containsExactly("categoria");
        }
    }

    @Nested
    class Ordenacao {

        final ArgumentCaptor<Sort> ordenacao = ArgumentCaptor.forClass(Sort.class);

        Sort ordenacaoRecebida(String uri) {
            when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(), i -> ScrollPosition.keyset()));
            assertThat(mvc.get().uri(uri).exchange()).hasStatusOk();
            verify(produtos).listar(any(), ordenacao.capture(), any(), anyInt());
            return ordenacao.getValue();
        }

        @Test
        void semSortOrdenaPeloNomeEDesempataPeloId() {
            assertThat(ordenacaoRecebida("/v1/produtos")).isEqualTo(Sort.by("nome", "id"));
        }

        @Test
        void traduzOsNomesDoContratoParaAsPropriedadesDaEntidade() {
            assertThat(ordenacaoRecebida("/v1/produtos?sort=precoEmCentavos,desc&sort=nome"))
                    .isEqualTo(Sort.by(Sort.Order.desc("preco.valor"), Sort.Order.asc("nome"), Sort.Order.asc("id")));
        }

        @Test
        void variosCamposNaMesmaDirecaoNumParametroSo() {
            assertThat(ordenacaoRecebida("/v1/produtos?sort=criadoEm,sku,desc"))
                    .isEqualTo(Sort.by(Sort.Order.desc("criadoEm"), Sort.Order.desc("sku"), Sort.Order.asc("id")));
        }

        @Test
        void recusaUmCampoForaDaLista() {
            // preco é o nome da propriedade na entidade, não no contrato; id também não é um campo ordenável.
            assertThat(mvc.get().uri("/v1/produtos?sort=preco,desc&sort=id").header("Accept-Language", "pt-BR").exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().isLenientlyEqualTo("""
                            { "type": "https://api.bazar.example/problemas/ordenacao-invalida",
                              "detail": "Ordenação não aceita: preco; id. Ordene por até 3 campos diferentes entre: criadoEm, nome, precoEmCentavos, sku.",
                              "ordenacao": ["preco", "id"],
                              "camposAceitos": ["criadoEm", "nome", "precoEmCentavos", "sku"],
                              "maximoDeCampos": 3 }
                            """);
            verifyNoInteractions(produtos);
        }

        @Test
        void umaDirecaoInvalidaViraUmCampoDesconhecido() {
            // O Spring só reconhece asc e desc no fim; "decrescente" é lido como mais uma propriedade.
            assertThat(mvc.get().uri("/v1/produtos?sort=nome,decrescente").exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().extractingPath("$.ordenacao").asArray().containsExactly("decrescente");
        }

        @Test
        void recusaIgnoreCaseCampoRepetidoECamposDemais() {
            assertThat(mvc.get().uri("/v1/produtos?sort=nome,ignorecase").exchange()).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(mvc.get().uri("/v1/produtos?sort=nome&sort=nome,desc").exchange()).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(mvc.get().uri("/v1/produtos?sort=nome&sort=sku&sort=criadoEm&sort=precoEmCentavos").exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().extractingPath("$.ordenacao").asArray().containsExactly("precoEmCentavos");
            verifyNoInteractions(produtos);
        }

        @Test
        void aBuscaAceitaOSortNaQuery() {
            when(produtos.listar(any(), any(), any(), anyInt())).thenReturn(Window.from(List.of(), i -> ScrollPosition.keyset()));

            var resposta = mvc.post().uri("/v1/produtos/buscas?sort=precoEmCentavos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .exchange();

            assertThat(resposta).hasStatusOk();
            verify(produtos).listar(any(), ordenacao.capture(), any(), anyInt());
            assertThat(ordenacao.getValue()).isEqualTo(Sort.by("preco.valor", "id"));
        }
    }

    @Nested
    class ComRestTestClient {

        @Autowired
        RestTestClient cliente;

        @Test
        void criaUmProdutoCom201ELocation() {
            when(produtos.criar(any())).thenAnswer(chamada -> chamada.getArgument(0));

            cliente.post().uri("/v1/produtos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""
                            { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                              "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                            """)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().valueMatches(HttpHeaders.LOCATION, "http://localhost/v1/produtos/[0-9a-f-]{36}")
                    .expectBody()
                    .jsonPath("$.sku").isEqualTo("LIV-0042")
                    .jsonPath("$.status").isEqualTo("RASCUNHO");
        }

        @Test
        void responde422ComOsErrosDeCadaCampo() {
            cliente.post().uri("/v1/produtos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                    .body("""
                            { "sku": "liv-42", "nome": "Clean Code", "precoEmCentavos": -5,
                              "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                            """)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .json("""
                            { "type": "https://api.bazar.example/problemas/validacao", "codigo": "VALIDACAO",
                              "erros": [
                                { "campo": "precoEmCentavos", "mensagem": "deve ser maior que 0" },
                                { "campo": "sku", "mensagem": "deve seguir o formato AAA-0000 (ex.: LIV-0042)" } ] }
                            """);
            verifyNoInteractions(produtos);
        }
    }
}
