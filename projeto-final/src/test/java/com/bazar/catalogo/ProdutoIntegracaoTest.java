package com.bazar.catalogo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import org.springframework.transaction.support.TransactionTemplate;

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

    @Autowired
    TransactionTemplate transacao;

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

    // A janela que a 11.04 deixou aberta: os dois cadastros passam pela consulta existsBySku, e só a constraint
    // decide. A transação do "outro" insere o SKU e fica aberta; o INSERT da API espera pelo índice unique e falha
    // quando o outro confirma. Sem a tradução da constraint, a resposta seria 500.
    @Test
    void doisCadastrosSimultaneosComOMesmoSkuDao409ParaOSegundo() throws Exception {
        var inseriu = new CountDownLatch(1);
        var confirmar = new CountDownLatch(1);
        try (var threads = Executors.newFixedThreadPool(2)) {
            var outro = threads.submit(() -> transacao.executeWithoutResult(status -> {
                jdbc.sql("""
                        insert into produto (id, sku, nome, preco_valor, preco_moeda, categoria_id, status, versao,
                                             criado_em, atualizado_em)
                        values (gen_random_uuid(), 'LIV-0042', 'Outro', 10, 'BRL',
                                '0192f3a0-5555-7000-8000-000000000003', 'RASCUNHO', 0, now(), now())
                        """).update();
                inseriu.countDown();
                aguardar(confirmar);
            }));
            inseriu.await();
            var api = threads.submit(() -> cliente.post().uri("/v1/produtos")
                    .contentType(MediaType.APPLICATION_JSON).body(CLEAN_CODE)
                    .exchange().returnResult());
            // Espera o INSERT da API ficar bloqueado no índice, e só então deixa o outro confirmar.
            while (jdbc.sql("select count(*) from pg_stat_activity where wait_event_type = 'Lock'")
                    .query(Integer.class).single() == 0) {
                Thread.onSpinWait();
            }
            confirmar.countDown();
            outro.get(10, TimeUnit.SECONDS);

            assertThat(api.get(10, TimeUnit.SECONDS).getStatus()).isEqualTo(HttpStatus.CONFLICT);
        }
        assertThat(jdbc.sql("select count(*) from produto").query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void comPreferReturnMinimalResponde201SemCorpo() {
        var resposta = cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Prefer", "return=minimal")
                .body(CLEAN_CODE)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Preference-Applied", "return=minimal")
                .expectBody().isEmpty();

        assertThat(resposta.getResponseHeaders().getLocation()).isNotNull();
    }

    // Os campos que o servidor controla (id, status, datas) não estão no CriarProdutoRequest: o Jackson os ignora,
    // e o produto nasce com os valores do servidor (14.01).
    @Test
    void ignoraOsCamposSomenteLeituraEnviadosNaCriacao() {
        cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CLEAN_CODE.replace("{ \"sku\"", """
                        { "id": "00000000-0000-0000-0000-000000000001", "status": "ATIVO",
                          "criadoEm": "2020-01-01T00:00:00Z", "sku\""""))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(id -> assertThat(id).isNotEqualTo("00000000-0000-0000-0000-000000000001"))
                .jsonPath("$.status").isEqualTo("RASCUNHO")
                .jsonPath("$.criadoEm").value(criadoEm -> assertThat((String) criadoEm).doesNotStartWith("2020"));
    }

    private static void aguardar(CountDownLatch sinal) {
        try {
            sinal.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    // Soft delete (14.03): o produto continua no banco e na API, DESCONTINUADO; repetir o DELETE dá o mesmo 204.
    @Test
    void deleteDescontinuaOProdutoEPodeSerRepetido() {
        var location = cliente.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(CLEAN_CODE)
                .exchange()
                .returnResult()
                .getResponseHeaders()
                .getLocation();

        cliente.delete().uri(location).exchange().expectStatus().isNoContent();
        cliente.delete().uri(location).exchange().expectStatus().isNoContent();

        cliente.get().uri(location)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("DESCONTINUADO");
        cliente.delete().uri("/v1/produtos/0192f39f-2222-7000-8000-000000000999")
                .exchange()
                .expectStatus().isNotFound();
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
