package com.bazar.catalogo;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

// Um teste de integração: a aplicação inteira num Tomcat de verdade, numa porta aleatória, com o PostgreSQL
// num contêiner. O RestTestClient é o mesmo do slice (11.03), mas aqui as requisições passam pela rede.
@TesteDeIntegracao
class ProdutoIntegracaoTest {

    static final String CLEAN_CODE = """
            { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
              "categoriaId": "0192f3a0-5555-7000-8000-000000000003", "atributos": { "autor": "Robert C. Martin" } }
            """;

    @Autowired
    RestTestClient cliente;

    @Autowired
    JdbcClient jdbc;

    @LocalServerPort
    int porta;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void gravaOProdutoEOEncontraPeloLocation() {
        var location = cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CLEAN_CODE)
                .exchange()
                .expectStatus().isCreated()
                .returnResult()
                .getResponseHeaders().getLocation();

        assertThat(location).hasToString("http://localhost:" + porta + location.getPath());
        cliente.get().uri(location)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.sku").isEqualTo("LIV-0042")
                .jsonPath("$.atributos.autor").isEqualTo("Robert C. Martin");
        cliente.get().uri(location + "/estoque")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.quantidadeDisponivel").isEqualTo(0);
    }

    @Test
    void recusaUmSkuRepetidoCom409() {
        cliente.post().uri("/v1/produtos").contentType(MediaType.APPLICATION_JSON).body(CLEAN_CODE)
                .exchange().expectStatus().isCreated();

        cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                .body(CLEAN_CODE)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .json("""
                        { "type": "https://api.bazar.example/problemas/sku-duplicado", "status": 409,
                          "codigo": "SKU_DUPLICADO", "sku": "LIV-0042",
                          "detail": "Já existe um produto com o SKU LIV-0042." }
                        """);
        assertThat(jdbc.sql("select count(*) from produto").query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void responde404ParaUmIdQueNaoEstaNoBanco() {
        cliente.get().uri("/v1/produtos/0192f39f-2222-7000-8000-000000000999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("PRODUTO_NAO_ENCONTRADO");
    }

    @Test
    void recusaUmaCategoriaQueNaoEstaNoBanco() {
        cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CLEAN_CODE.replace("000000000003", "000000000999"))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody()
                .jsonPath("$.erros[0].campo").isEqualTo("categoriaId");
    }
}
