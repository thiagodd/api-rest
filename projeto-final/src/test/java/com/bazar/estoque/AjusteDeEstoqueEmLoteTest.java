package com.bazar.estoque;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

// Ajuste de estoque em lote (14.04): parcial, com o resultado de cada item, pela rede e até o banco.
@TesteDeIntegracao
class AjusteDeEstoqueEmLoteTest {

    static final String INEXISTENTE = "0192f39f-2222-7000-8000-000000000999";

    @Autowired
    RestTestClient cliente;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    // O item do meio falha (produto inexistente); os outros dois são aplicados e ficam no banco.
    @Test
    void aplicaOsItensValidosEDescreveOQueFalhou() {
        var livro = criarProduto("LIV-0042");
        var caneca = criarProduto("CAN-0001");

        lote("""
                { "itens": [
                    { "produtoId": "%s", "quantidadeDisponivel": 30 },
                    { "produtoId": "%s", "quantidadeDisponivel": 5 },
                    { "produtoId": "%s", "quantidadeDisponivel": 12 } ] }
                """.formatted(livro, INEXISTENTE, caneca))
                .expectStatus().isEqualTo(HttpStatus.MULTI_STATUS)
                .expectBody().jsonPath("$.sucessos").isEqualTo(2);

        assertThat(disponivel(livro)).isEqualTo(30);
        assertThat(disponivel(caneca)).isEqualTo(12);
    }

    @Test
    void oResultadoTemUmItemPorItemNaMesmaOrdem() {
        var livro = criarProduto("LIV-0042");
        lote("""
                { "itens": [
                    { "produtoId": "%s", "quantidadeDisponivel": 30 },
                    { "produtoId": "%s", "quantidadeDisponivel": 5 } ] }
                """.formatted(INEXISTENTE, livro))
                .expectStatus().isEqualTo(HttpStatus.MULTI_STATUS)
                .expectBody()
                .jsonPath("$.sucessos").isEqualTo(1)
                .jsonPath("$.falhas").isEqualTo(1)
                .jsonPath("$.itens[0].indice").isEqualTo(0)
                .jsonPath("$.itens[0].status").isEqualTo(404)
                .jsonPath("$.itens[0].problema.type").isEqualTo("https://api.bazar.example/problemas/produto-nao-encontrado")
                .jsonPath("$.itens[0].estoque").doesNotExist()
                .jsonPath("$.itens[1].status").isEqualTo(200)
                .jsonPath("$.itens[1].estoque.quantidadeDisponivel").isEqualTo(5)
                .jsonPath("$.itens[1].problema").doesNotExist();
    }

    // Mesmo com todos os itens falhando, o lote foi processado: 207, e não 404.
    @Test
    void umLoteEmQueTudoFalhaContinuaSendo207() {
        lote("""
                { "itens": [ { "produtoId": "%s", "quantidadeDisponivel": 5 } ] }
                """.formatted(INEXISTENTE))
                .expectStatus().isEqualTo(HttpStatus.MULTI_STATUS)
                .expectBody().jsonPath("$.falhas").isEqualTo(1);
    }

    // Um lote que nem pode começar é recusado inteiro, antes de aplicar qualquer item.
    @Test
    void recusaUmLoteComMaisDe500Itens() {
        var itens = IntStream.range(0, 501)
                .mapToObj(i -> "{ \"produtoId\": \"%s\", \"quantidadeDisponivel\": 1 }".formatted(UUID.randomUUID()))
                .collect(Collectors.joining(","));
        lote("{ \"itens\": [" + itens + "] }")
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.erros[0].campo").isEqualTo("itens");
    }

    @Test
    void recusaOMesmoProdutoDuasVezesNoLote() {
        var livro = criarProduto("LIV-0042");
        lote("""
                { "itens": [
                    { "produtoId": "%s", "quantidadeDisponivel": 30 },
                    { "produtoId": "%s", "quantidadeDisponivel": 5 } ] }
                """.formatted(livro, livro))
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.erros[0].campo").isEqualTo("itens[1].produtoId");
        assertThat(disponivel(livro)).isZero();
    }

    private RestTestClient.ResponseSpec lote(String corpo) {
        return cliente.post().uri("/v1/ajustes-estoque/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                .body(corpo)
                .exchange();
    }

    private String criarProduto(String sku) {
        var location = cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "sku": "%s", "nome": "Produto %s", "precoEmCentavos": 1000,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """.formatted(sku, sku))
                .exchange().returnResult().getResponseHeaders().getLocation();
        var caminho = location.getPath();
        return caminho.substring(caminho.lastIndexOf('/') + 1);
    }

    private int disponivel(String produtoId) {
        return jdbc.sql("select quantidade_disponivel from estoque where produto_id = ?")
                .param(UUID.fromString(produtoId))
                .query(Integer.class).single();
    }
}
