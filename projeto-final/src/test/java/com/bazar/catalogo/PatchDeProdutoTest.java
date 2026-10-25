package com.bazar.catalogo;

import java.net.URI;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

// PATCH com JSON Merge Patch e JSON Patch (14.02), pela rede e até o banco.
@TesteDeIntegracao
class PatchDeProdutoTest {

    static final MediaType MERGE_PATCH = MediaType.parseMediaType("application/merge-patch+json");
    static final MediaType JSON_PATCH = MediaType.parseMediaType("application/json-patch+json");

    @Autowired
    RestTestClient cliente;

    @Autowired
    JdbcClient jdbc;

    URI produto;

    @BeforeEach
    void criarProduto() {
        produto = cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "descricao": "O clássico", "precoEmCentavos": 12990,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                          "atributos": { "autor": "Robert C. Martin", "paginas": "464" } }
                        """)
                .exchange().returnResult().getResponseHeaders().getLocation();
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void mergePatchMudaSoOQueFoiEnviado() {
        patch(MERGE_PATCH, """
                { "precoEmCentavos": 9990 }
                """)
                .expectStatus().isOk()
                .expectBody().json("""
                        { "nome": "Clean Code", "descricao": "O clássico", "precoEmCentavos": 9990,
                          "atributos": { "autor": "Robert C. Martin", "paginas": "464" } }
                        """);
    }

    // null remove; num objeto (atributos), o merge desce campo a campo.
    @Test
    void mergePatchComNullRemoveOCampo() {
        patch(MERGE_PATCH, """
                { "descricao": null, "atributos": { "paginas": null, "editora": "Alta Books" } }
                """)
                .expectStatus().isOk()
                .expectBody().json("""
                        { "descricao": null, "atributos": { "autor": "Robert C. Martin", "editora": "Alta Books" } }
                        """)
                .jsonPath("$.atributos.paginas").doesNotExist();
    }

    // O resultado do patch passa pelas validações do PUT: remover o nome deixa o produto inválido.
    @Test
    void validaOResultadoDepoisDeAplicar() {
        patch(MERGE_PATCH, """
                { "nome": null }
                """)
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.erros[0].campo").isEqualTo("nome");
        cliente.get().uri(produto).exchange().expectBody().jsonPath("$.nome").isEqualTo("Clean Code");
    }

    // sku e status não fazem parte do que um PATCH edita: 422, e não um sucesso que não mudou nada.
    @Test
    void recusaUmCampoQueNaoPodeSerAlterado() {
        patch(MERGE_PATCH, """
                { "sku": "LIV-9999" }
                """)
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.erros[0].campo").isEqualTo("sku");
    }

    @Test
    void jsonPatchComTestAplicaQuandoOValorConfere() {
        patch(JSON_PATCH, """
                [ { "op": "test", "path": "/precoEmCentavos", "value": 12990 },
                  { "op": "replace", "path": "/precoEmCentavos", "value": 9990 },
                  { "op": "add", "path": "/atributos/editora", "value": "Alta Books" } ]
                """)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.precoEmCentavos").isEqualTo(9990)
                .jsonPath("$.atributos.editora").isEqualTo("Alta Books");
    }

    // Outra pessoa mudou o preço antes: o test falha, e nenhuma operação é aplicada.
    @Test
    void jsonPatchComTestQueFalhaDa409ENaoMudaNada() {
        patch(JSON_PATCH, """
                [ { "op": "replace", "path": "/nome", "value": "Clean Code (2ª edição)" },
                  { "op": "test", "path": "/precoEmCentavos", "value": 11990 },
                  { "op": "replace", "path": "/precoEmCentavos", "value": 9990 } ]
                """)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.type").isEqualTo("https://api.bazar.example/problemas/teste-do-patch-falhou")
                .jsonPath("$.operacao").isEqualTo(1);
        cliente.get().uri(produto).exchange().expectBody()
                .jsonPath("$.nome").isEqualTo("Clean Code")
                .jsonPath("$.precoEmCentavos").isEqualTo(12990);
    }

    @Test
    void jsonPatchMalformadoEhUm400EUmaOperacaoImpossivelEhUm422() {
        patch(JSON_PATCH, """
                [ { "op": "trocar", "path": "/nome", "value": "x" } ]
                """)
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.type").isEqualTo("https://api.bazar.example/problemas/patch-invalido");
        patch(JSON_PATCH, """
                [ { "op": "remove", "path": "/atributos/isbn" } ]
                """)
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT)
                .expectBody().jsonPath("$.erros[0].campo").isEqualTo("/atributos/isbn");
    }

    // PATCH com application/json: o Bazar não sabe se o corpo é um merge patch ou outra coisa.
    @Test
    void recusaUmPatchSemOTipoDoDocumento() {
        patch(MediaType.APPLICATION_JSON, """
                { "precoEmCentavos": 9990 }
                """)
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .expectHeader().value("Accept-Patch", aceitos -> assertThat(aceitos)
                        .contains("application/merge-patch+json").contains("application/json-patch+json"));
    }

    private RestTestClient.ResponseSpec patch(MediaType tipo, String corpo) {
        return cliente.patch().uri(produto)
                .contentType(tipo)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                .body(corpo)
                .exchange();
    }
}
