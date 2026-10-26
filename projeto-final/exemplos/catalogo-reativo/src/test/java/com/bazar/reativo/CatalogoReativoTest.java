package com.bazar.reativo;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "spring.sql.init.mode=always")
@AutoConfigureWebTestClient
@Import(CatalogoReativoTest.Banco.class)
class CatalogoReativoTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class Banco {

        // O @ServiceConnection também entrega a URL r2dbc:postgresql://... quando o driver R2DBC está no classpath.
        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer("postgres:18");
        }
    }

    @Autowired
    WebTestClient http;

    @Autowired
    DatabaseClient banco;

    @Test
    void buscaOProdutoPeloId() {
        http.get().uri("/v1/produtos/bcd399f5-d6d1-4032-9c80-10ca4c363d31").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sku").isEqualTo("LIV-0042")
                .jsonPath("$.preco.valor").isEqualTo(139.90)
                .jsonPath("$.preco.moeda").isEqualTo("BRL");
    }

    @Test
    void produtoInexistenteRespondeProblemDetails() {
        http.get().uri("/v1/produtos/0192f3a1-9999-7000-8000-000000000123").exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://api.bazar.example/problemas/produto-nao-encontrado")
                .jsonPath("$.codigo").isEqualTo("PRODUTO_NAO_ENCONTRADO");
    }

    @Test
    void nadaAconteceAteAlguemAssinar() {
        // Montar o Mono não toca o banco: o insert só roda na assinatura.
        var insercao = banco.sql("""
                insert into produto values ('0192f3a1-0000-7000-8000-000000000001', 'LIV-0099', 'Teste', null, 10.00,
                'BRL', '0192f3a0-5555-7000-8000-000000000003', 'RASCUNHO', now())
                """).fetch().rowsUpdated();
        assertThat(total()).isEqualTo(2L);

        StepVerifier.create(insercao).expectNext(1L).expectComplete().verify(Duration.ofSeconds(5));
        assertThat(total()).isEqualTo(3L);

        banco.sql("delete from produto where sku = 'LIV-0099'").fetch().rowsUpdated().block();
    }

    // block() num teste é aceitável: aqui não há event loop para travar.
    private Long total() {
        return banco.sql("select count(*) as total from produto").map(linha -> linha.get("total", Long.class))
                .one().block();
    }

    @Test
    void aListagemSaiUmProdutoPorLinha() {
        var corpo = http.get().uri("/v1/produtos").accept(MediaType.APPLICATION_NDJSON).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
                .expectBody(String.class).returnResult().getResponseBody();

        assertThat(corpo).isNotNull();
        assertThat(corpo.lines()).hasSize(2).first().asString().contains("\"sku\":\"LIV-0042\"");
    }
}
