package com.bazar.catalogo.api;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ETag e requisições condicionais (20.02).
 */
@TesteDeIntegracao
class EtagTest {

    static final String PROGRAMACAO = "0192f3a0-5555-7000-8000-000000000003";

    @Autowired
    RestTestClient cliente;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void oProdutoNaoMudouEntao304SemCorpo() {
        var produto = umProduto();
        var etag = cliente.get().uri(produto).exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ETAG, "\"0\"")
                .returnResult().getResponseHeaders().getETag();

        cliente.get().uri(produto).header(HttpHeaders.IF_NONE_MATCH, etag).exchange()
                .expectStatus().isNotModified()
                // O 304 repete os headers de cache: o cliente renova a cópia que tem.
                .expectHeader().valueEquals(HttpHeaders.ETAG, "\"0\"")
                .expectHeader().exists(HttpHeaders.CACHE_CONTROL)
                .expectBody().isEmpty();
    }

    @Test
    void depoisDeUmaAlteracaoOEtagAntigoNaoValeMais() {
        var produto = umProduto();
        cliente.put().uri(produto).contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "nome": "Clean Code", "precoEmCentavos": 11990, "categoriaId": "%s" }
                        """.formatted(PROGRAMACAO))
                .exchange()
                .expectStatus().isOk();

        cliente.get().uri(produto).header(HttpHeaders.IF_NONE_MATCH, "\"0\"").exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ETAG, "\"1\"")
                .expectBody().jsonPath("$.precoEmCentavos").isEqualTo(11990);
    }

    @Test
    void ifModifiedSinceComOLastModifiedTambemDa304() {
        var produto = umProduto();
        var ultimaModificacao = cliente.get().uri(produto).exchange()
                .expectStatus().isOk()
                .returnResult().getResponseHeaders().getFirst(HttpHeaders.LAST_MODIFIED);

        cliente.get().uri(produto).header(HttpHeaders.IF_MODIFIED_SINCE, ultimaModificacao).exchange()
                .expectStatus().isNotModified();
        // Uma data anterior: o produto mudou depois dela.
        var ontem = DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.now().minusSeconds(86_400).atOffset(ZoneOffset.UTC));
        cliente.get().uri(produto).header(HttpHeaders.IF_MODIFIED_SINCE, ontem).exchange()
                .expectStatus().isOk();
    }

    @Test
    void comACategoriaDentroOEtagLevaAsDuasVersoes() {
        var produto = umProduto();
        cliente.get().uri(produto + "?embed=categoria").exchange()
                .expectStatus().isOk()
                .expectHeader().valueMatches(HttpHeaders.ETAG, "\"0-\\d+\"")
                .expectHeader().doesNotExist(HttpHeaders.LAST_MODIFIED);
    }

    @Test
    void asCategoriasTemEtagPeloHashDoCorpo() {
        var etag = cliente.get().uri("/v1/categorias").exchange()
                .expectStatus().isOk()
                .returnResult().getResponseHeaders().getETag();
        // O ShallowEtagHeaderFilter usa o MD5 do corpo, com um "0" na frente.
        assertThat(etag).matches("\"0[0-9a-f]{32}\"");

        cliente.get().uri("/v1/categorias").header(HttpHeaders.IF_NONE_MATCH, etag).exchange()
                .expectStatus().isNotModified()
                .expectBody().isEmpty();
    }

    private URI umProduto() {
        return cliente.post().uri("/v1/produtos").contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990, "categoriaId": "%s" }
                        """.formatted(PROGRAMACAO))
                .exchange()
                .expectStatus().isCreated()
                .returnResult().getResponseHeaders().getLocation();
    }
}
