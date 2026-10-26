package com.bazar.integracoes.webhooks;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.estoque.dominio.EstoqueRepository;
import com.bazar.integracoes.AssinaturaStandardWebhooks;
import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Webhooks de saída (21.03): um servidor HTTP de verdade, em localhost, no papel do parceiro.
 */
@TesteDeIntegracao
class WebhooksTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    record Recebido(String id, String timestamp, String assinatura, byte[] corpo) {
    }

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ProdutoService produtos;

    @Autowired
    EstoqueRepository estoques;

    @Autowired
    ClienteRepository clientes;

    @Autowired
    JdbcClient jdbc;

    HttpServer parceiro;
    final List<Recebido> recebidos = new CopyOnWriteArrayList<>();
    final AtomicInteger proximoStatus = new AtomicInteger(200);
    UUID clienteId;
    UUID cleanCode;

    @BeforeEach
    void subirOParceiro() throws IOException {
        parceiro = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        parceiro.createContext("/webhooks", troca -> {
            var cabecalhos = troca.getRequestHeaders();
            recebidos.add(new Recebido(cabecalhos.getFirst("webhook-id"), cabecalhos.getFirst("webhook-timestamp"),
                    cabecalhos.getFirst("webhook-signature"), troca.getRequestBody().readAllBytes()));
            troca.sendResponseHeaders(proximoStatus.get(), -1);
            troca.close();
        });
        parceiro.start();

        clienteId = clientes.save(Cliente.novo("Ana Souza", "ana@example.com", "52998224725", null, Canal.WEB)).id();
        cleanCode = produtos.criar(new Produto("LIV-0042", "Clean Code", null, Dinheiro.deCentavos(12990, "BRL"),
                PROGRAMACAO, Map.of())).getId();
        var estoque = estoques.findById(cleanCode).orElseThrow();
        estoque.repor(10);
        estoques.save(estoque);
    }

    @AfterEach
    void derrubarOParceiro() {
        parceiro.stop(0);
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void oSegredoSoApareceNaCriacao() throws Exception {
        var criada = assinar();
        assertThat(criada.segredo()).startsWith("whsec_");

        assertThat(mvc.get().uri("/v1/assinaturas-webhook/{id}", criada.id())).hasStatusOk()
                .bodyJson().doesNotHavePath("$.segredo");
    }

    @Test
    void umEventoNaoAssinadoNaoEhEntregueEUmEventoDesconhecidoEh422() {
        assertThat(mvc.post().uri("/v1/assinaturas-webhook").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "parceiro": "ERP", "url": "http://localhost:1/webhooks", "eventos": [ "pedido.pago" ] }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.erros[0].campo").isEqualTo("eventos[0]");
    }

    @Test
    void oParceiroRecebeOPedidoCriadoAssinado() throws Exception {
        var assinatura = assinar();
        criarPedido();

        await().atMost(Duration.ofSeconds(5)).until(() -> !recebidos.isEmpty());
        var recebido = recebidos.getFirst();
        // O parceiro confere com o segredo que recebeu na criação, como faria do lado dele.
        assertThat(new AssinaturaStandardWebhooks(assinatura.segredo()).verificar(recebido.id(), recebido.timestamp(),
                recebido.corpo(), recebido.assinatura(), Instant.now(), Duration.ofMinutes(5))).isTrue();
        var corpo = new String(recebido.corpo());
        assertThat((String) JsonPath.read(corpo, "$.type")).isEqualTo("pedido.criado");
        assertThat((String) JsonPath.read(corpo, "$.data.valorTotal.valor")).isEqualTo("129.90");

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(
                mvc.get().uri("/v1/assinaturas-webhook/{id}/entregas", assinatura.id()))
                .bodyJson().isLenientlyEqualTo("""
                        [ { "tipoEvento": "pedido.criado", "status": "ENTREGUE", "tentativas": 1,
                            "ultimoStatusHttp": 200 } ]
                        """));
    }

    @Test
    void umaFalhaAgendaOutraTentativaComOMesmoWebhookId() throws Exception {
        proximoStatus.set(503);
        var assinatura = assinar();
        criarPedido();

        await().atMost(Duration.ofSeconds(5)).until(() -> recebidos.size() == 1);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(entrega(assinatura))
                .containsEntry("status", "PENDENTE").containsEntry("ultimo_status_http", 503));
        // A primeira espera é de 5 s, com variação de 75% a 125%.
        var espera = Duration.between(Instant.now(), ((Timestamp) entrega(assinatura)
                .get("proxima_tentativa_em")).toInstant());
        assertThat(espera).isBetween(Duration.ofSeconds(2), Duration.ofSeconds(7));

        proximoStatus.set(200);
        jdbc.sql("update entrega_webhook set proxima_tentativa_em = now()").update();
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(entrega(assinatura))
                .containsEntry("status", "ENTREGUE").containsEntry("tentativas", 2));
        // O mesmo evento, a mesma identidade: é assim que o parceiro reconhece uma repetição.
        assertThat(recebidos).extracting(Recebido::id).containsExactly(recebidos.getFirst().id(),
                recebidos.getFirst().id());
    }

    @Test
    void um410DesativaAAssinatura() throws Exception {
        proximoStatus.set(410);
        var assinatura = assinar();
        criarPedido();

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(
                mvc.get().uri("/v1/assinaturas-webhook/{id}", assinatura.id()))
                .bodyJson().extractingPath("$.ativa").isEqualTo(false));
        assertThat(entrega(assinatura)).containsEntry("status", "FALHOU");
    }

    record Criada(UUID id, String segredo) {
    }

    private Criada assinar() throws Exception {
        var resposta = mvc.post().uri("/v1/assinaturas-webhook").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "parceiro": "ERP da Loja Exemplo", "url": "http://localhost:%d/webhooks",
                          "eventos": [ "pedido.criado" ] }
                        """.formatted(parceiro.getAddress().getPort()))
                .exchange();
        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        var corpo = resposta.getResponse().getContentAsString();
        return new Criada(UUID.fromString(JsonPath.read(corpo, "$.id")), JsonPath.read(corpo, "$.segredo"));
    }

    private void criarPedido() {
        assertThat(mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "clienteId": "%s", "itens": [ { "produtoId": "%s", "quantidade": 1 } ] }
                        """.formatted(clienteId, cleanCode)))
                .hasStatus(HttpStatus.CREATED);
    }

    private Map<String, Object> entrega(Criada assinatura) {
        return jdbc.sql("select * from entrega_webhook where assinatura_id = ?").param(assinatura.id())
                .query().singleRow();
    }
}
