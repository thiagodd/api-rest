package com.bazar.catalogo.api;

import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.DinheiroMapperImpl;
import com.bazar.compartilhado.web.AplicadorDePatch;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Gera os trechos (snippets) do guia de produtos em target/generated-snippets (17.05). Um campo da resposta que não
 * esteja descrito aqui, ou um campo descrito que não exista, faz o teste falhar: o guia não fica para trás do código.
 */
@WebMvcTest(ProdutoController.class)
@AutoConfigureRestDocs
@Import({ ProdutoMapperImpl.class, DinheiroMapperImpl.class, AplicadorDePatch.class })
class DocumentacaoDeProdutosTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProdutoService produtos;

    @Test
    void detalharUmProduto() throws Exception {
        var produto = umProduto().comAtributo("autor", "Robert C. Martin").comVersao(0).build();
        when(produtos.detalhar(produto.getId())).thenReturn(produto);

        mvc.perform(get("/v1/produtos/{id}", produto.getId()))
                .andExpect(status().isOk())
                .andDo(document("detalhar-produto",
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("id").description("O id do produto (UUID).")),
                        queryParameters(parameterWithName("embed").optional()
                                .description("`categoria` traz a categoria dentro do produto.")),
                        responseFields(
                                fieldWithPath("id").description("O id do produto."),
                                fieldWithPath("sku").description("O código do lojista, único no catálogo."),
                                fieldWithPath("nome").description("O nome exibido na vitrine."),
                                // Sem o tipo, o REST Docs o deduz do exemplo, e o null daria "Null". O optional aceita o null.
                                fieldWithPath("descricao").type(JsonFieldType.STRING).optional()
                                        .description("A descrição longa. `null` quando não há."),
                                fieldWithPath("precoEmCentavos")
                                        .description("O preço em centavos de real. Depreciado: use `preco`."),
                                fieldWithPath("preco.valor").description("O preço, decimal em texto (`129.90`)."),
                                fieldWithPath("preco.moeda").description("A moeda, em ISO 4217 (`BRL`)."),
                                fieldWithPath("categoriaId").description("A categoria do produto."),
                                fieldWithPath("status")
                                        .description("`RASCUNHO`, `ATIVO` ou `DESCONTINUADO`. Lista extensível."),
                                subsectionWithPath("atributos")
                                        .description("Pares nome e valor livres, como o autor de um livro."),
                                fieldWithPath("criadoEm").description("Quando o produto foi cadastrado (UTC)."),
                                fieldWithPath("atualizadoEm").description("A última alteração (UTC)."))));
    }
}
