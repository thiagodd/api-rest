package com.bazar.catalogo.api;

import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.DinheiroMapperImpl;
import com.bazar.compartilhado.web.AplicadorDePatch;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.ApiVersionInserter;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// As duas versões do produto lado a lado (18.03): o mesmo serviço, duas representações, e o 400 de uma versão que
// não existe.
@WebMvcTest({ ProdutoController.class, ProdutoV2Controller.class })
@AutoConfigureRestTestClient
@Import({ ProdutoMapperImpl.class, ProdutoV2MapperImpl.class, DinheiroMapperImpl.class, AplicadorDePatch.class })
class VersoesDoProdutoTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    RestTestClient cliente;

    @MockitoBean
    ProdutoService produtos;

    @Test
    void aV1MantemOsCentavosEAV2SoTemODinheiro() {
        var produto = umProduto().comPreco(12990).build();
        when(produtos.detalhar(produto.getId())).thenReturn(produto);

        assertThat(mvc.get().uri("/v1/produtos/{id}", produto.getId())).hasStatusOk()
                .bodyJson().satisfies(json -> {
                    json.assertThat().extractingPath("$.precoEmCentavos").isEqualTo(12990);
                    json.assertThat().extractingPath("$.preco.valor").isEqualTo("129.90");
                });
        assertThat(mvc.get().uri("/v2/produtos/{id}", produto.getId())).hasStatusOk()
                .bodyJson().satisfies(json -> {
                    json.assertThat().doesNotHavePath("$.precoEmCentavos");
                    json.assertThat().extractingPath("$.preco.valor").isEqualTo("129.90");
                });
    }

    @Test
    void umaVersaoQueNaoExisteEh400ComOTipoDoCatalogo() {
        assertThat(mvc.get().uri("/v3/produtos/{id}", umProduto().build().getId()))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().satisfies(json -> {
                    json.assertThat().extractingPath("$.codigo").isEqualTo("VERSAO_NAO_SUPORTADA");
                    json.assertThat().extractingPath("$.detail")
                            .isEqualTo("A versão '3.0.0' não existe, ou este recurso não a tem.");
                });
    }

    @Test
    void umSegmentoQueNaoEhUmaVersaoTambemEh400() {
        // Sem o predicado amplo (/v[^/]*/), /v1.5 ficaria sem versão e o {versao} do mapeamento aceitaria o valor.
        assertThat(mvc.get().uri("/v1.5/produtos")).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(mvc.get().uri("/vabc/produtos")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void umaVersaoQueORecursoNaoTemTambemEh400() {
        // A v2 existe, mas a listagem ainda não foi para a v2: só o detalhe foi (31.09 completa).
        assertThat(mvc.get().uri("/v2/produtos"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.codigo").isEqualTo("VERSAO_NAO_SUPORTADA");
    }

    @Test
    void oClienteDizAVersaoEOInserterMontaOCaminho() {
        var produto = umProduto().build();
        when(produtos.detalhar(produto.getId())).thenReturn(produto);
        var v2 = cliente.mutate()
                .apiVersionInserter(ApiVersionInserter.builder()
                        .usePathSegment(0)
                        .withVersionFormatter(versao -> "v" + versao)
                        .build())
                .build();

        v2.get().uri("/produtos/{id}", produto.getId()).apiVersion(2)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.precoEmCentavos").doesNotExist();
    }
}
