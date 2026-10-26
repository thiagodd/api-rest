package com.bazar.pedidos.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.estoque.dominio.EstoqueRepository;
import com.bazar.pedidos.dominio.ItemSolicitado;
import com.bazar.pedidos.dominio.MotivoCancelamento;
import com.bazar.pedidos.dominio.PedidoService;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * O acompanhamento do pedido por SSE (19.04), por HTTP de verdade: o MockMvc não mantém a conexão aberta como um
 * EventSource.
 */
@TesteDeIntegracao
class EventosDoPedidoTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    @LocalServerPort
    int porta;

    @Autowired
    PedidoService pedidos;

    @Autowired
    ProdutoService produtos;

    @Autowired
    EstoqueRepository estoques;

    @Autowired
    ClienteRepository clientes;

    @Autowired
    AcompanhamentoDePedidos acompanhamento;

    @Autowired
    JdbcClient jdbc;

    final HttpClient http = HttpClient.newHttpClient();

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void quemAcompanhaRecebeOHistoricoEOCancelamentoAoVivo() throws Exception {
        var pedido = umPedido();

        // A conexão fica aberta até o pedido chegar a um estado final; o corpo inteiro só termina aí.
        var fluxo = http.sendAsync(eventos(pedido, null), HttpResponse.BodyHandlers.ofString());
        await().atMost(Duration.ofSeconds(5)).until(() -> acompanhamento.abertas() == 1);

        pedidos.cancelar(pedido, MotivoCancelamento.CLIENTE_DESISTIU, null);

        var resposta = fluxo.get(5, TimeUnit.SECONDS);
        assertThat(resposta.statusCode()).isEqualTo(200);
        assertThat(resposta.headers().firstValue("Content-Type")).hasValue("text/event-stream");
        assertThat(resposta.headers().firstValue("X-Accel-Buffering")).hasValue("no");
        assertThat(status(resposta.body())).containsExactly("CRIADO", "CANCELADO");
        assertThat(resposta.body()).contains("retry:3000");
        // O servidor encerrou o fluxo, e a inscrição saiu do registro.
        await().atMost(Duration.ofSeconds(5)).until(() -> acompanhamento.abertas() == 0);
    }

    @Test
    void aoReconectarComLastEventIdSoChegamOsEventosSeguintes() throws Exception {
        var pedido = umPedido();
        pedidos.cancelar(pedido, MotivoCancelamento.CLIENTE_DESISTIU, null);
        var ids = jdbc.sql("select id from historico_pedido where pedido_id = ? order by id").param(pedido)
                .query(Long.class).list();

        var resposta = http.send(eventos(pedido, ids.get(0)), HttpResponse.BodyHandlers.ofString());

        assertThat(status(resposta.body())).containsExactly("CANCELADO");
        assertThat(resposta.body()).contains("id:" + ids.get(1));
    }

    @Test
    void depoisDoUltimoEventoDeUmPedidoFinalAReconexaoRecebe204() throws Exception {
        var pedido = umPedido();
        pedidos.cancelar(pedido, MotivoCancelamento.CLIENTE_DESISTIU, null);
        var ultimo = jdbc.sql("select max(id) from historico_pedido where pedido_id = ?").param(pedido)
                .query(Long.class).single();

        // Pela especificação do HTML, o 204 faz o EventSource parar de reconectar.
        var resposta = http.send(eventos(pedido, ultimo), HttpResponse.BodyHandlers.ofString());

        assertThat(resposta.statusCode()).isEqualTo(204);
    }

    @Test
    void pedidoInexistenteRecebe404() throws Exception {
        var resposta = http.send(eventos(UUID.randomUUID(), null), HttpResponse.BodyHandlers.ofString());

        assertThat(resposta.statusCode()).isEqualTo(404);
        assertThat(resposta.headers().firstValue("Content-Type")).hasValue("application/problem+json");
    }

    private HttpRequest eventos(UUID pedido, @Nullable Long ultimoVisto) {
        var requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/v1/pedidos/" + pedido + "/eventos"))
                .header("Accept", "text/event-stream");
        if (ultimoVisto != null) {
            requisicao.header("Last-Event-ID", ultimoVisto.toString());
        }
        return requisicao.build();
    }

    private static List<String> status(String corpo) {
        return corpo.lines().filter(linha -> linha.startsWith("data:"))
                .map(linha -> linha.replaceAll(".*\"status\":\"([A-Z_]+)\".*", "$1"))
                .toList();
    }

    private UUID umPedido() {
        var cliente = clientes.save(Cliente.novo("Ana Souza", "ana@example.com", "52998224725", null, Canal.WEB)).id();
        var produto = produtos.criar(new Produto("LIV-0077", "Java Concurrency in Practice", null,
                Dinheiro.deCentavos(18990, "BRL"), PROGRAMACAO, Map.of()));
        var estoque = estoques.findById(produto.getId()).orElseThrow();
        estoque.repor(10);
        estoques.save(estoque);
        return pedidos.criar(cliente, List.of(new ItemSolicitado(produto.getId(), 1))).getId();
    }
}
