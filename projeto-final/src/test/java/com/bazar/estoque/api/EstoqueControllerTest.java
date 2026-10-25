package com.bazar.estoque.api;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bazar.LimpezaDoBanco;
import com.bazar.TestcontainersConfiguration;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EstoqueControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ProdutoService produtos;

    @Autowired
    JdbcClient jdbc;

    UUID produtoId;

    @BeforeEach
    void criarProduto() {
        produtoId = produtos.criar(new Produto("LIV-0042", "Clean Code", null, Dinheiro.deCentavos(12990, "BRL"),
                UUID.fromString("0192f3a0-5555-7000-8000-000000000003"), Map.of())).getId();
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void defineOSaldoAPartirDaVersaoLida() {
        assertThat(mvc.get().uri("/v1/produtos/{id}/estoque", produtoId)).hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        { "quantidadeDisponivel": 0, "quantidadeReservada": 0, "versao": 0 }
                        """);

        assertThat(definir(10, 0)).hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        { "quantidadeDisponivel": 10, "versao": 1 }
                        """);
    }

    @Test
    void recusaUmaVersaoDesatualizadaCom409() {
        assertThat(definir(10, 0)).hasStatusOk();

        // Outro cliente ainda tem a versão 0.
        assertThat(mvc.put().uri("/v1/produtos/{id}/estoque", produtoId).contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-BR")
                .content("""
                        { "quantidadeDisponivel": 3, "versao": 0 }
                        """))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/conflito-de-versao",
                          "title": "Conflito de versão",
                          "detail": "O recurso foi alterado por outra requisição. Leia de novo e repita a alteração.",
                          "codigo": "CONFLITO_DE_VERSAO", "versaoAtual": 1 }
                        """);
        assertThat(mvc.get().uri("/v1/produtos/{id}/estoque", produtoId))
                .bodyJson().extractingPath("$.quantidadeDisponivel").isEqualTo(10);
    }

    @Test
    void deDuasAlteracoesSimultaneasDaMesmaVersaoSoUmaVence() throws Exception {
        for (int rodada = 0; rodada < 10; rodada++) {
            var versao = jdbc.sql("select versao from estoque where produto_id = ?").param(produtoId).query(Long.class).single();
            var largada = new CountDownLatch(1);
            var respostas = new ArrayList<Future<Integer>>();
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < 2; i++) {
                    // Valores sempre novos: se nada mudasse, o Hibernate não faria UPDATE, e não haveria conflito.
                    var quantidade = 100 + rodada * 2 + i;
                    respostas.add(executor.submit(() -> {
                        largada.await();
                        return definir(quantidade, versao).getResponse().getStatus();
                    }));
                }
                largada.countDown();
                var status = new ArrayList<Integer>();
                for (var resposta : respostas) {
                    status.add(resposta.get());
                }
                // O perdedor recebe 409 pela conferência no serviço ou pelo @Version no commit.
                assertThat(status).containsExactlyInAnyOrder(200, 409);
            }
        }
    }

    @Test
    void validaOCorpo() {
        assertThat(mvc.put().uri("/v1/produtos/{id}/estoque", produtoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "quantidadeDisponivel": -1 }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.erros[*].campo").asArray()
                .containsExactlyInAnyOrder("quantidadeDisponivel", "versao");
    }

    @Test
    void responde404ParaUmProdutoInexistente() {
        assertThat(mvc.get().uri("/v1/produtos/{id}/estoque", UUID.randomUUID())).hasStatus(HttpStatus.NOT_FOUND);
    }

    private org.springframework.test.web.servlet.assertj.MvcTestResult definir(int quantidade, long versao) {
        return mvc.put().uri("/v1/produtos/{id}/estoque", produtoId).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "quantidadeDisponivel": %d, "versao": %d }
                        """.formatted(quantidade, versao))
                .exchange();
    }
}
