package com.bazar.catalogo.api;

import java.util.Map;

import com.bazar.catalogo.dominio.ProdutoRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ProdutoController.class)
@Import(ProdutoRepository.class)
class ProdutoControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void criaUmProdutoComoRascunho() {
        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "descricao": "Um guia de boas práticas.",
                          "precoEmCentavos": 12990, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                          "atributos": { "autor": "Robert C. Martin" } }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        assertThat(resposta.getResponse().getHeader("Location")).startsWith("http://localhost/v1/produtos/");
        assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                  "status": "RASCUNHO", "atributos": { "autor": "Robert C. Martin" } }
                """);
        assertThat(resposta).bodyJson().doesNotHavePath("$.versao");
    }

    @Test
    void ignoraOsCamposQueOClienteNaoControla() {
        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "id": "0192f39f-2222-7000-8000-000000000042", "sku": "LIV-0043", "nome": "Refactoring",
                          "precoEmCentavos": 15900, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                          "status": "ATIVO", "versao": 99, "criadoEm": "2020-01-01T00:00:00Z" }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        assertThat(resposta).bodyJson().extractingPath("$.status").isEqualTo("RASCUNHO");
        assertThat(resposta).bodyJson().extractingPath("$.id").isNotEqualTo("0192f39f-2222-7000-8000-000000000042");
        assertThat(resposta).bodyJson().extractingPath("$.criadoEm").asString().doesNotStartWith("2020");
        assertThat(resposta).bodyJson().extractingPath("$.atributos").isEqualTo(Map.of());
    }

    @Test
    void atualizaUmProdutoSemMudarOSku() {
        var location = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "LIV-0044", "nome": "DDD", "precoEmCentavos": 19900,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """)
                .exchange().getResponse().getHeader("Location");
        var caminho = location.substring("http://localhost".length());

        assertThat(mvc.put().uri(caminho)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Domain-Driven Design", "descricao": null, "precoEmCentavos": 17990,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003", "atributos": { "autor": "Eric Evans" } }
                        """))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        { "sku": "LIV-0044", "nome": "Domain-Driven Design", "precoEmCentavos": 17990 }
                        """);
    }

    @Test
    void responde404ParaUmProdutoInexistente() {
        assertThat(mvc.get().uri("/v1/produtos/0192f39f-2222-7000-8000-000000000999"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
