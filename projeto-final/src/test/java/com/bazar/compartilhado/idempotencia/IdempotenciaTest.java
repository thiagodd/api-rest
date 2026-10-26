package com.bazar.compartilhado.idempotencia;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Idempotency keys no POST /v1/pedidos (21.01).
 */
@TesteDeIntegracao
class IdempotenciaTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ProdutoService produtos;

    @Autowired
    EstoqueRepository estoques;

    @Autowired
    ClienteRepository clientes;

    @Autowired
    ChavesDeIdempotencia chaves;

    @Autowired
    TransactionTemplate transacao;

    @Autowired
    JdbcClient jdbc;

    UUID clienteId;
    UUID cleanCode;

    @BeforeEach
    void prepararCatalogo() {
        clienteId = clientes.save(Cliente.novo("Ana Souza", "ana@example.com", "52998224725", null, Canal.WEB)).id();
        var produto = produtos.criar(new Produto("LIV-0042", "Clean Code", null, Dinheiro.deCentavos(12990, "BRL"),
                PROGRAMACAO, Map.of()));
        cleanCode = produto.getId();
        repor(5);
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void aMesmaChaveDevolveAMesmaRespostaSemCriarOutroPedido() {
        var primeira = criarPedido("\"8e03978e-40d5-43e8-bc93-6894a57f9324\"", 2);
        assertThat(primeira).hasStatus(HttpStatus.CREATED).doesNotContainHeader(IdempotenciaFilter.IDEMPOTENT_REPLAYED);

        var repeticao = criarPedido("\"8e03978e-40d5-43e8-bc93-6894a57f9324\"", 2);
        assertThat(repeticao).hasStatus(HttpStatus.CREATED)
                .hasHeader(IdempotenciaFilter.IDEMPOTENT_REPLAYED, "true")
                .hasHeader("Location", primeira.getResponse().getHeader("Location"));
        assertThat(repeticao.getResponse().getContentAsByteArray())
                .isEqualTo(primeira.getResponse().getContentAsByteArray());

        // Um pedido só, e o estoque reservado uma vez só.
        assertThat(jdbc.sql("select count(*) from pedido").query(Long.class).single()).isEqualTo(1);
        assertThat(estoques.findById(cleanCode).orElseThrow().getQuantidadeReservada()).isEqualTo(2);
    }

    @Test
    void aMesmaChaveComOutroCorpoRecebe422() {
        assertThat(criarPedido("\"chave-1\"", 2)).hasStatus(HttpStatus.CREATED);

        assertThat(criarPedido("\"chave-1\"", 3)).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/chave-de-idempotencia-reutilizada",
                          "codigo": "CHAVE_DE_IDEMPOTENCIA_REUTILIZADA" }
                        """);
    }

    @Test
    void enquantoAPrimeiraEstaEmAndamentoAMesmaChaveRecebe409() throws Exception {
        // Outra transação segura o lock da chave, como faria a primeira requisição ainda em andamento.
        var travou = new CountDownLatch(1);
        var soltar = new CountDownLatch(1);
        var primeira = CompletableFuture.runAsync(() -> transacao.executeWithoutResult(status -> {
            assertThat(chaves.travar("POST /v1/pedidos", "chave-1")).isTrue();
            travou.countDown();
            try {
                soltar.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }));
        travou.await(10, TimeUnit.SECONDS);

        assertThat(criarPedido("\"chave-1\"", 2)).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().isLenientlyEqualTo("""
                        { "codigo": "REQUISICAO_EM_ANDAMENTO" }
                        """);
        soltar.countDown();
        primeira.get(10, TimeUnit.SECONDS);

        // Solto o lock, a mesma chave é processada.
        assertThat(criarPedido("\"chave-1\"", 2)).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void umErroNaoFicaGuardadoEAMesmaChavePodeSerRepetida() {
        assertThat(criarPedido("\"chave-1\"", 8)).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.codigo").isEqualTo("ESTOQUE_INSUFICIENTE");
        assertThat(jdbc.sql("select count(*) from chave_idempotencia").query(Long.class).single()).isZero();

        repor(10);
        assertThat(criarPedido("\"chave-1\"", 8)).hasStatus(HttpStatus.CREATED)
                .doesNotContainHeader(IdempotenciaFilter.IDEMPOTENT_REPLAYED);
    }

    @Test
    void semAChaveCadaPostCriaUmPedido() {
        for (int i = 0; i < 2; i++) {
            assertThat(mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON).content(corpo(1)))
                    .hasStatus(HttpStatus.CREATED);
        }
        assertThat(jdbc.sql("select count(*) from pedido").query(Long.class).single()).isEqualTo(2);
    }

    @Test
    void umaChaveVaziaOuLongaDemaisRecebe400() {
        assertThat(criarPedido("\"\"", 1)).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.codigo").isEqualTo("CHAVE_DE_IDEMPOTENCIA_INVALIDA");
        assertThat(criarPedido("x".repeat(256), 1)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void umaChaveVencidaNaoValeMaisEEApagadaNaLimpeza() {
        var ontem = Instant.now().minusSeconds(86_400);
        chaves.guardar("POST /v1/pedidos", "velha", new RespostaGuardada("abc", 201, Map.of(), new byte[0]),
                ontem.minusSeconds(60), ontem);

        assertThat(chaves.buscar("POST /v1/pedidos", "velha", Instant.now())).isEmpty();
        assertThat(chaves.apagarVencidas(Instant.now())).isEqualTo(1);
    }

    private MvcTestResult criarPedido(String chave, int quantidade) {
        return mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                .header(IdempotenciaFilter.IDEMPOTENCY_KEY, chave)
                .content(corpo(quantidade))
                .exchange();
    }

    private String corpo(int quantidade) {
        return """
                { "clienteId": "%s", "itens": [ { "produtoId": "%s", "quantidade": %d } ] }
                """.formatted(clienteId, cleanCode, quantidade);
    }

    private void repor(int quantidade) {
        var estoque = estoques.findById(cleanCode).orElseThrow();
        estoque.repor(quantidade);
        estoques.save(estoque);
    }
}
