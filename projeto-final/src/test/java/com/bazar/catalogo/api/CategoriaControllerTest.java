package com.bazar.catalogo.api;

import java.util.Map;
import java.util.UUID;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import static org.assertj.core.api.Assertions.assertThat;

// Um teste de integração (11.04): a aplicação inteira com o PostgreSQL. O contrato HTTP isolado, sem banco,
// é testado com @WebMvcTest (11.03).
// Desde 10.02 as categorias estão no PostgreSQL, com as três categorias iniciais inseridas pelo Flyway.
@TesteDeIntegracao
@ExtendWith(OutputCaptureExtension.class)
class CategoriaControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ProdutoService produtos;

    @Autowired
    JdbcClient jdbc;

    // O banco é compartilhado pelos testes: cada um desfaz o que criou, e as categorias iniciais ficam.
    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void naoRemoveUmaCategoriaComProdutos() {
        var programacao = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");
        produtos.criar(new Produto("LIV-0042", "Clean Code", null, Dinheiro.deCentavos(12990, "BRL"), programacao, Map.of()));

        assertThat(mvc.delete().uri("/v1/categorias/{id}", programacao).header("Accept-Language", "pt-BR"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/categoria-com-produtos",
                          "title": "A categoria possui produtos", "status": 409,
                          "detail": "A categoria \\"Programação\\" tem 1 produto(s). Mova ou descontinue os produtos antes de removê-la.",
                          "codigo": "CATEGORIA_COM_PRODUTOS", "quantidadeDeProdutos": 1 }
                        """);
    }

    @Test
    void auditaARemocaoEMedeOTempo(CapturedOutput saida) {
        var location = mvc.post().uri("/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Brinquedos", "slug": "brinquedos", "categoriaPaiId": null }
                        """)
                .exchange().getResponse().getHeader(HttpHeaders.LOCATION);
        var id = location.substring(location.lastIndexOf('/') + 1);

        assertThat(mvc.delete().uri(location).header("Bazar-Canal", "app")).hasStatus(HttpStatus.NO_CONTENT);

        // A aplicação inteira inclui os filtros (@Component Filter) e a ConfiguracaoWeb com o interceptor.
        assertThat(saida).contains("acao=categoria.remover metodo=DELETE caminho=/v1/categorias/" + id
                + " status=204 canal=APP");
        assertThat(saida).containsPattern("DELETE /v1/categorias/" + id + " → 204 em \\d+ ms");
    }

    @Test
    void naoAuditaUmaLeitura(CapturedOutput saida) {
        assertThat(mvc.get().uri("/v1/categorias")).hasStatusOk();

        assertThat(saida).doesNotContain("bazar.auditoria");
    }

    @Test
    void naoRemoveUmaCategoriaComSubcategorias() {
        // Livros é a categoria pai de Programação.
        assertThat(mvc.delete().uri("/v1/categorias/0192f3a0-5555-7000-8000-000000000001").header("Accept-Language", "pt-BR"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/categoria-com-subcategorias",
                          "detail": "A categoria \\"Livros\\" tem 1 subcategoria(s). Mova ou remova as subcategorias antes de removê-la.",
                          "codigo": "CATEGORIA_COM_SUBCATEGORIAS", "quantidadeDeSubcategorias": 1 }
                        """);
    }

    @Test
    void listaAsCategoriasDentroDeItens() {
        assertThat(mvc.get().uri("/v1/categorias"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .extractingPath("$.itens[*].nome")
                .asArray()
                .containsExactly("Eletrônicos", "Livros", "Programação");
    }

    @Test
    void filtraPeloNomeEPelasRaizes() {
        assertThat(mvc.get().uri("/v1/categorias?q=liv"))
                .bodyJson().extractingPath("$.itens[*].slug").asArray().containsExactly("livros");
        assertThat(mvc.get().uri("/v1/categorias?apenasRaizes=true"))
                .bodyJson().extractingPath("$.itens[*].slug").asArray().containsExactly("eletronicos", "livros");
    }

    @Test
    void detalhaUmaCategoriaComCategoriaPaiNulaExplicita() {
        assertThat(mvc.get().uri("/v1/categorias/0192f3a0-5555-7000-8000-000000000001"))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        { "id": "0192f3a0-5555-7000-8000-000000000001", "nome": "Livros",
                          "slug": "livros", "categoriaPaiId": null }
                        """);
    }

    @Test
    void responde404ParaUmIdInexistente() {
        assertThat(mvc.get().uri("/v1/categorias/0192f3a0-5555-7000-8000-000000000999"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void responde400ParaUmIdQueNaoEUuid() {
        assertThat(mvc.get().uri("/v1/categorias/abc")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejeitaUmSlugForaDoFormato() {
        assertThat(mvc.post().uri("/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Games", "slug": "Jogos Eletrônicos" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void validaOParametroDeBuscaPeloMetodo() {
        var resposta = mvc.get().uri("/v1/categorias?q=a").exchange();

        assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getMvcResult().getResolvedException()).isInstanceOf(HandlerMethodValidationException.class);
    }

    @Test
    void criaSubstituiERemoveUmaCategoria() {
        var criacao = mvc.post().uri("/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Games", "slug": "games", "categoriaPaiId": null }
                        """)
                .exchange();
        assertThat(criacao).hasStatus(HttpStatus.CREATED)
                .bodyJson().extractingPath("$.nome").isEqualTo("Games");
        var location = criacao.getResponse().getHeader("Location");
        assertThat(location).startsWith("http://localhost/v1/categorias/");
        var caminho = location.substring("http://localhost".length());

        assertThat(mvc.get().uri(caminho)).hasStatusOk();

        assertThat(mvc.put().uri(caminho)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Jogos", "slug": "jogos", "categoriaPaiId": null }
                        """))
                .hasStatusOk()
                .bodyJson().extractingPath("$.slug").isEqualTo("jogos");

        assertThat(mvc.delete().uri(caminho)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(mvc.delete().uri(caminho)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.get().uri(caminho)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void responde404AoSubstituirUmaCategoriaInexistente() {
        assertThat(mvc.put().uri("/v1/categorias/0192f3a0-5555-7000-8000-000000000999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "X", "slug": "x", "categoriaPaiId": null }
                        """))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void respondeOptionsComOsMetodosDaColecao() {
        var resposta = mvc.options().uri("/v1/categorias").exchange();
        assertThat(resposta).hasStatusOk();
        // A ordem dos métodos no Allow não é garantida
        assertThat(resposta.getResponse().getHeader("Allow").split(","))
                .containsExactlyInAnyOrder("GET", "HEAD", "POST", "OPTIONS");
    }
}
