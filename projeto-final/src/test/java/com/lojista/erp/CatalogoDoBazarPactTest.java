package com.lojista.erp;

import java.util.Map;
import java.util.UUID;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.web.client.RestClient;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * O teste de consumidor do ERP do lojista (18.05). O Pact sobe um servidor falso do Bazar com as respostas descritas
 * aqui, o cliente do ERP conversa com ele, e o que o ERP realmente usou vira o contrato: target/pacts/
 * erp-do-lojista-bazar-api.json, que o ERP publica no broker.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "bazar-api", pactVersion = PactSpecVersion.V4)
class CatalogoDoBazarPactTest {

    static final UUID CLEAN_CODE = UUID.fromString("01a17566-c2c2-750b-a6fd-e44a3e704746");
    static final UUID REMOVIDO = UUID.fromString("01a17566-0000-7000-8000-000000000000");

    @Pact(consumer = "erp-do-lojista")
    V4Pact produtoExistente(PactDslWithProvider bazar) {
        return bazar.given("existe o produto LIV-0042")
                .uponReceiving("o detalhe de um produto pela v1")
                .path("/v1/produtos/" + CLEAN_CODE)
                .method("GET")
                .headers("Accept", "application/json")
                .willRespondWith()
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(newJsonBody(produto -> {
                    produto.stringMatcher("sku", "[A-Z]{3}-\\d{4}", "LIV-0042");
                    produto.stringType("nome", "Clean Code");
                    produto.integerType("precoEmCentavos", 12990);
                }).build())
                .toPact(V4Pact.class);
    }

    @Pact(consumer = "erp-do-lojista")
    V4Pact produtoInexistente(PactDslWithProvider bazar) {
        return bazar.given("não existe o produto")
                .uponReceiving("o detalhe de um produto que não existe")
                .path("/v1/produtos/" + REMOVIDO)
                .method("GET")
                .headers("Accept", "application/json")
                .willRespondWith()
                .status(404)
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "produtoExistente")
    void importaSkuNomeEPreco(MockServer bazar) {
        var catalogo = new CatalogoDoBazar(RestClient.create(bazar.getUrl()));

        assertThat(catalogo.produto(CLEAN_CODE)).contains(new ProdutoNoErp("LIV-0042", "Clean Code", 12990));
    }

    @Test
    @PactTestFor(pactMethod = "produtoInexistente")
    void umProdutoQueNaoExisteMaisFicaVazio(MockServer bazar) {
        var catalogo = new CatalogoDoBazar(RestClient.create(bazar.getUrl()));

        assertThat(catalogo.produto(REMOVIDO)).isEmpty();
    }
}
