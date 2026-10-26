package com.bazar.compartilhado.web;

import java.util.UUID;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * O Cache-Control de cada tipo de resposta (20.01): cache só onde o controller declarou, no-store no resto.
 */
@TesteDeIntegracao
class PoliticaDeCacheTest {

    static final String PROGRAMACAO = "0192f3a0-5555-7000-8000-000000000003";

    static final String DETALHE_DO_PRODUTO = "max-age=30, public, s-maxage=60, stale-while-revalidate=30";

    @Autowired
    RestTestClient cliente;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void categoriasPodemFicarEmQualquerCachePorDezMinutos() {
        cliente.get().uri("/v1/categorias").exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL,
                        "max-age=600, public, stale-if-error=86400, stale-while-revalidate=60");
        cliente.get().uri("/v1/categorias/" + PROGRAMACAO).exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL,
                        "max-age=600, public, stale-if-error=86400, stale-while-revalidate=60");
    }

    @Test
    void oDetalheDoProdutoFicaMaisTempoNoCacheCompartilhadoQueNoNavegador() {
        var location = cliente.post().uri("/v1/produtos").contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                          "categoriaId": "%s" }
                        """.formatted(PROGRAMACAO))
                .exchange()
                .expectStatus().isCreated()
                // A resposta de uma escrita nunca vai para cache.
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, "no-store")
                .returnResult().getResponseHeaders().getLocation();

        cliente.get().uri(location).exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, DETALHE_DO_PRODUTO);
        cliente.get().uri(location.getPath().replace("/v1/", "/v2/")).exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, DETALHE_DO_PRODUTO);
    }

    // Com a compressão do Tomcat ligada (22.03), ele acrescenta accept-encoding ao Vary, e reescreve a lista em
    // minúsculas: nomes de header não diferenciam maiúsculas.
    @Test
    void listasQueSaemEmJsonOuCsvAvisamOCacheComVaryAccept() {
        cliente.get().uri("/v1/produtos").exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.VARY, "accept,accept-encoding");
        cliente.get().uri("/v1/produtos").accept(MediaType.parseMediaType("text/csv")).exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.VARY, "accept,accept-encoding");
    }

    @Test
    void errosEDadosDeClienteNaoVaoParaCache() {
        cliente.get().uri("/v1/produtos/" + UUID.randomUUID()).exchange()
                .expectStatus().isNotFound()
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, "no-store");
        cliente.get().uri("/v1/pedidos/" + UUID.randomUUID()).exchange()
                .expectStatus().isNotFound()
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, "no-store");
        var maria = cliente.post().uri("/v1/clientes").contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "nome": "Maria Souza", "email": "maria@example.com", "cpf": "52998224725" }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .returnResult().getResponseHeaders().getLocation();
        // Os dados de um cliente: nem o navegador deveria guardá-los.
        cliente.get().uri(maria).exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.CACHE_CONTROL, "no-store");
    }
}
