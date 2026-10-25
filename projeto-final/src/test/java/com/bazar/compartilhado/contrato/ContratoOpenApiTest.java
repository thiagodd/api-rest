package com.bazar.compartilhado.contrato;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O contrato OpenAPI fica versionado em docs/openapi/bazar-api.yaml (17.02). Este teste gera o contrato a partir do
 * código e o compara com o arquivo: uma mudança na API que muda o contrato quebra o build até alguém atualizar o
 * arquivo, e a mudança aparece no diff do PR, onde é revisada (17.04).
 *
 * <p>Para atualizar o arquivo: {@code ./mvnw test -Dtest=ContratoOpenApiTest -Dcontrato.atualizar=true}.
 */
@TesteDeIntegracao
class ContratoOpenApiTest {

    static final Path ARQUIVO = Path.of("docs/openapi/bazar-api.yaml");

    @Autowired
    RestTestClient cliente;

    @Test
    void oContratoVersionadoEhOGeradoPeloCodigo() throws IOException {
        var gerado = cliente.get().uri("/v3/api-docs.yaml")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody();

        if (Boolean.getBoolean("contrato.atualizar")) {
            Files.writeString(ARQUIVO, gerado);
        }
        assertThat(ARQUIVO).exists();
        assertThat(gerado)
                .as("O contrato mudou. Rode ./mvnw test -Dtest=ContratoOpenApiTest -Dcontrato.atualizar=true "
                        + "e revise o diff de " + ARQUIVO)
                .isEqualTo(Files.readString(ARQUIVO));
    }
}
