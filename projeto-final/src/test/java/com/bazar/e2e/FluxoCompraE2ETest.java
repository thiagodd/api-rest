package com.bazar.e2e;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.report.LevelResolver;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

/**
 * Fluxos completos pela perspectiva de um cliente da API: só HTTP, sem acesso a beans nem ao banco (a não ser
 * para limpar no fim). Cada cenário cria os próprios dados, com um SKU único, e não depende de nada que outro
 * teste tenha deixado.
 */
@TesteDeIntegracao
class FluxoCompraE2ETest {

    static final String PROGRAMACAO = "0192f3a0-5555-7000-8000-000000000003";

    // O que toda resposta de erro do Bazar tem (08.01).
    static final ResponseSpecification PROBLEMA = new ResponseSpecBuilder()
            .expectContentType("application/problem+json")
            .expectBody("type", matchesPattern("https://api\\.bazar\\.example/problemas/[a-z-]+"))
            .expectBody("traceId", matchesPattern("[0-9a-f]{32}"))
            .build();

    @LocalServerPort
    int porta;

    @Autowired
    JdbcClient jdbc;

    // Toda resposta destes testes é conferida contra o contrato versionado (17.03). As requisições não: alguns
    // cenários mandam requisições inválidas de propósito, para ver o erro.
    static final OpenApiValidationFilter CONTRATO = new OpenApiValidationFilter(
            OpenApiInteractionValidator.createForSpecificationUrl("docs/openapi/bazar-api.yaml")
                    .withLevelResolver(LevelResolver.create()
                            .withLevel("validation.request", ValidationReport.Level.IGNORE)
                            .build())
                    .build());

    RequestSpecification api;

    @BeforeEach
    void configurarOCliente() {
        api = new RequestSpecBuilder()
                .setBaseUri("http://localhost")
                .setPort(porta)
                .setBasePath("/v1")
                .setContentType(ContentType.JSON)
                .addHeader("Accept-Language", "pt-BR")
                .addFilter(CONTRATO)
                .build();
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void compraCompleta() {
        var clienteId = cadastrarCliente();
        var sku = skuUnico();
        var produtoId = cadastrarProduto(sku, 12990);
        definirEstoque(produtoId, 10);

        var pedidoId = given(api)
                .body(Map.of("clienteId", clienteId, "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 2))))
        .when()
                .post("/pedidos")
        .then()
                .statusCode(201)
                .header("Location", matchesPattern("http://localhost:\\d+/v1/pedidos/[0-9a-f-]{36}"))
                .body("status", equalTo("CRIADO"))
                .body("itens[0].sku", equalTo(sku))
                .body("itens[0].precoUnitario.valor", equalTo("129.90"))
                .body("valorTotal.valor", equalTo("259.80"))
                .extract().<String>path("id");

        given(api)
        .when()
                .get("/produtos/{id}/estoque", produtoId)
        .then()
                .statusCode(200)
                .body("quantidadeDisponivel", equalTo(8))
                .body("quantidadeReservada", equalTo(2));

        given(api)
                .queryParam("clienteId", clienteId)
        .when()
                .get("/pedidos")
        .then()
                .statusCode(200)
                .body("itens.id", contains(pedidoId));
    }

    @Test
    void pedidoMaiorQueOEstoqueNaoReservaNada() {
        var clienteId = cadastrarCliente();
        var produtoId = cadastrarProduto(skuUnico(), 5000);
        definirEstoque(produtoId, 1);

        given(api)
                .body(Map.of("clienteId", clienteId, "itens", List.of(Map.of("produtoId", produtoId, "quantidade", 2))))
        .when()
                .post("/pedidos")
        .then()
                .statusCode(409)
                .spec(PROBLEMA)
                .body("codigo", equalTo("ESTOQUE_INSUFICIENTE"))
                .body("quantidadeDisponivel", equalTo(1))
                .body("quantidadeSolicitada", equalTo(2));

        given(api)
        .when()
                .get("/produtos/{id}/estoque", produtoId)
        .then()
                .body("quantidadeDisponivel", equalTo(1))
                .body("quantidadeReservada", equalTo(0));
    }

    // Os passos do cenário, como um cliente da API os faria.

    private String cadastrarCliente() {
        return given(api)
                .body(Map.of("nome", "Maria Souza", "email", "maria@example.com", "cpf", "52998224725"))
        .when()
                .post("/clientes")
        .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String cadastrarProduto(String sku, int precoEmCentavos) {
        return given(api)
                .body(Map.of("sku", sku, "nome", "Clean Code", "precoEmCentavos", precoEmCentavos,
                        "categoriaId", PROGRAMACAO))
        .when()
                .post("/produtos")
        .then()
                .statusCode(201)
                .extract().path("id");
    }

    // O PUT do estoque exige a versão lida (10.06): primeiro o GET, depois o PUT com ela.
    private void definirEstoque(String produtoId, int quantidade) {
        int versao = given(api)
                .get("/produtos/{id}/estoque", produtoId)
        .then()
                .statusCode(200)
                .extract().path("versao");

        given(api)
                .body(Map.of("quantidadeDisponivel", quantidade, "versao", versao))
        .when()
                .put("/produtos/{id}/estoque", produtoId)
        .then()
                .statusCode(200)
                .body("quantidadeDisponivel", equalTo(quantidade));
    }

    // Um SKU por cenário: dois cenários nunca disputam o mesmo produto, mesmo que a limpeza falhe.
    private static String skuUnico() {
        return "TST-%04d".formatted(ThreadLocalRandom.current().nextInt(10_000));
    }
}
