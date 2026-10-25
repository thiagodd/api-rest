package com.bazar.pedidos.api;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.bazar.ContadorDeConsultas;
import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.estoque.dominio.EstoqueRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManagerFactory;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;

// As estatísticas do Hibernate permitem contar as consultas de cada requisição (10.04).
@TesteDeIntegracao
@ExtendWith(OutputCaptureExtension.class)
class PedidoControllerTest {

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
    JdbcClient jdbc;

    @Autowired
    EntityManagerFactory emf;

    @Autowired
    LoggingSystem logs;

    UUID clienteId;
    UUID cleanCode;
    UUID refactoring;

    @BeforeEach
    void prepararCatalogo() {
        clienteId = clientes.save(Cliente.novo("Ana Souza", "ana@example.com", "52998224725", null, Canal.WEB)).id();
        cleanCode = criarProdutoComEstoque("LIV-0042", "Clean Code", 12990, 5);
        refactoring = criarProdutoComEstoque("LIV-0043", "Refactoring", 15900, 1);
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void criaOPedidoEReservaOEstoque(CapturedOutput saida) {
        var resposta = mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "clienteId": "%s",
                          "itens": [ { "produtoId": "%s", "quantidade": 2 }, { "produtoId": "%s", "quantidade": 1 } ] }
                        """.formatted(clienteId, cleanCode, refactoring))
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                { "status": "CRIADO",
                  "itens": [ { "sku": "LIV-0042", "quantidade": 2, "precoUnitario": { "valor": "129.90", "moeda": "BRL" },
                               "subtotal": { "valor": "259.80", "moeda": "BRL" } },
                             { "sku": "LIV-0043", "quantidade": 1, "subtotal": { "valor": "159.00", "moeda": "BRL" } } ],
                  "valorItens": { "valor": "418.80", "moeda": "BRL" },
                  "valorFrete": { "valor": "0.00", "moeda": "BRL" },
                  "valorTotal": { "valor": "418.80", "moeda": "BRL" } }
                """);
        assertThat(estoques.findById(cleanCode)).get()
                .returns(3, e -> e.getQuantidadeDisponivel())
                .returns(2, e -> e.getQuantidadeReservada());
        // O ouvinte AFTER_COMMIT rodou, porque a transação foi confirmada.
        assertThat(saida).contains("pedido.criado id=");

        var location = resposta.getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(mvc.get().uri(location)).hasStatusOk()
                .bodyJson().extractingPath("$.itens[*].sku").asArray().containsExactly("LIV-0042", "LIV-0043");
    }

    @Test
    void desfazTudoQuandoUmItemNaoTemEstoque(CapturedOutput saida) {
        // O primeiro item tem estoque e é reservado; o segundo pede 2 de um produto com 1.
        var resposta = mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-BR")
                .content("""
                        { "clienteId": "%s",
                          "itens": [ { "produtoId": "%s", "quantidade": 2 }, { "produtoId": "%s", "quantidade": 2 } ] }
                        """.formatted(clienteId, cleanCode, refactoring))
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/estoque-insuficiente",
                          "codigo": "ESTOQUE_INSUFICIENTE", "produtoId": "%s",
                          "quantidadeDisponivel": 1, "quantidadeSolicitada": 2 }
                        """.formatted(refactoring));
        // A reserva do primeiro item foi desfeita junto com o resto da transação.
        assertThat(estoques.findById(cleanCode)).get()
                .returns(5, e -> e.getQuantidadeDisponivel())
                .returns(0, e -> e.getQuantidadeReservada());
        assertThat(jdbc.sql("select count(*) from pedido").query(Long.class).single()).isZero();
        assertThat(saida).doesNotContain("pedido.criado id=");
    }

    @Test
    void recusaUmClienteOuUmProdutoQueNaoExistem() {
        assertThat(mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-BR")
                .content("""
                        { "clienteId": "%s", "itens": [ { "produtoId": "%s", "quantidade": 1 } ] }
                        """.formatted(UUID.randomUUID(), cleanCode)))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().isLenientlyEqualTo("""
                        { "erros": [ { "campo": "clienteId", "mensagem": "O cliente informado não existe." } ] }
                        """);

        assertThat(mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-BR")
                .content("""
                        { "clienteId": "%s", "itens": [ { "produtoId": "%s", "quantidade": 1 },
                                                        { "produtoId": "%s", "quantidade": 1 } ] }
                        """.formatted(clienteId, cleanCode, UUID.randomUUID())))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().isLenientlyEqualTo("""
                        { "erros": [ { "campo": "itens[1].produtoId", "mensagem": "O produto informado não existe." } ] }
                        """);
    }

    @Test
    void validaOCorpo() {
        assertThat(mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-BR")
                .content("""
                        { "clienteId": "%s", "itens": [ { "produtoId": "%s", "quantidade": 0 } ] }
                        """.formatted(clienteId, cleanCode)))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.erros[0].campo").isEqualTo("itens[0].quantidade");
    }

    @Test
    void listaOsPedidosDoClienteComUmaConsultaSo() {
        for (int i = 0; i < 5; i++) {
            mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "clienteId": "%s", "itens": [ { "produtoId": "%s", "quantidade": 1 } ] }
                            """.formatted(clienteId, cleanCode))
                    .exchange();
        }

        var consultas = new ContadorDeConsultas(emf).contar(() ->
                assertThat(mvc.get().uri("/v1/pedidos?clienteId={id}", clienteId)).hasStatusOk()
                        .bodyJson().extractingPath("$.itens[*].itens[0].sku").asArray().hasSize(5).containsOnly("LIV-0042"));

        // Pedidos e itens no mesmo SELECT. Sem o @EntityGraph, seriam 6: 1 + 1 por pedido.
        assertThat(consultas).isEqualTo(1);
    }

    @Test
    void responde404ParaUmPedidoInexistente() {
        assertThat(mvc.get().uri("/v1/pedidos/{id}", UUID.randomUUID())).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Nested
    class Cancelamento {

        String pedido;

        @BeforeEach
        void criarOPedido() {
            var resposta = mvc.post().uri("/v1/pedidos").contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "clienteId": "%s", "itens": [ { "produtoId": "%s", "quantidade": 2 } ] }
                            """.formatted(clienteId, cleanCode))
                    .exchange();
            pedido = resposta.getResponse().getHeader(HttpHeaders.LOCATION);
        }

        @Test
        void oPedidoCriadoOfereceOCancelamento() {
            assertThat(mvc.get().uri(pedido).accept(MediaTypes.HAL_JSON))
                    .hasStatusOk()
                    .hasContentType(MediaTypes.HAL_JSON)
                    .bodyJson().isLenientlyEqualTo("""
                            { "status": "CRIADO",
                              "_links": {
                                "self": { "href": "%1$s" },
                                "bazar:cliente": { "href": "http://localhost/v1/clientes/%2$s" },
                                "bazar:cancelar": { "href": "%1$s/cancelamento" },
                                "curies": [ { "name": "bazar", "href": "https://api.bazar.example/rels/{rel}",
                                              "templated": true } ] } }
                            """.formatted(pedido, clienteId));
        }

        @Test
        void cancelaELiberaOEstoque() {
            assertThat(mvc.post().uri(pedido + "/cancelamento").contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "motivo": "ENDERECO_ERRADO", "observacao": "Mudei de endereço" }
                            """))
                    .hasStatusOk()
                    .bodyJson()
                    .hasPathSatisfying("$.status", status -> assertThat(status).isEqualTo("CANCELADO"))
                    .doesNotHavePath("$._links['bazar:cancelar']");

            assertThat(estoques.findById(cleanCode)).get()
                    .returns(5, e -> e.getQuantidadeDisponivel())
                    .returns(0, e -> e.getQuantidadeReservada());
            assertThat(jdbc.sql("select cancelamento_motivo, cancelamento_observacao from pedido")
                    .query((rs, n) -> rs.getString(1) + "/" + rs.getString(2)).single())
                    .isEqualTo("ENDERECO_ERRADO/Mudei de endereço");
        }

        @Test
        void naoCancelaDuasVezes() {
            mvc.post().uri(pedido + "/cancelamento").contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"motivo\": \"CLIENTE_DESISTIU\" }").exchange();

            assertThat(mvc.post().uri(pedido + "/cancelamento").contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"motivo\": \"CLIENTE_DESISTIU\" }"))
                    .hasStatus(HttpStatus.CONFLICT)
                    .bodyJson().isLenientlyEqualTo("""
                            { "codigo": "TRANSICAO_INVALIDA", "statusAtual": "CANCELADO", "statusDesejado": "CANCELADO" }
                            """);
            // A segunda tentativa não devolveu ao estoque o que já tinha sido devolvido.
            assertThat(estoques.findById(cleanCode)).get().returns(5, e -> e.getQuantidadeDisponivel());
        }

        @Test
        void umPedidoPagoNaoOfereceNemAceitaOCancelamento() {
            jdbc.sql("update pedido set status = 'PAGO'").update();

            assertThat(mvc.get().uri(pedido)).hasStatusOk()
                    .bodyJson().doesNotHavePath("$._links['bazar:cancelar']");
            assertThat(mvc.post().uri(pedido + "/cancelamento").contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"motivo\": \"CLIENTE_DESISTIU\" }"))
                    .hasStatus(HttpStatus.CONFLICT)
                    .bodyJson().extractingPath("$.statusAtual").isEqualTo("PAGO");
            assertThat(estoques.findById(cleanCode)).get().returns(2, e -> e.getQuantidadeReservada());
        }

        @Test
        void validaOMotivo() {
            assertThat(mvc.post().uri(pedido + "/cancelamento").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                    .bodyJson().extractingPath("$.erros[0].campo").isEqualTo("motivo");
            // Um valor fora do enum nem chega à validação: o Jackson não consegue ler o corpo.
            assertThat(mvc.post().uri(pedido + "/cancelamento").contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"motivo\": \"TALVEZ\" }"))
                    .hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void comAcceptJsonORespostaEhHalComContentTypeJson() {
            assertThat(mvc.get().uri(pedido).accept(MediaType.APPLICATION_JSON))
                    .hasContentType(MediaType.APPLICATION_JSON)
                    .bodyJson().hasPath("$._links.self.href");
        }
    }

    @Test
    void cancelarUmPedidoInexistenteEh404() {
        assertThat(mvc.post().uri("/v1/pedidos/{id}/cancelamento", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content("{ \"motivo\": \"CLIENTE_DESISTIU\" }"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    private UUID criarProdutoComEstoque(String sku, String nome, long precoEmCentavos, int quantidade) {
        var produto = produtos.criar(new Produto(sku, nome, null, Dinheiro.deCentavos(precoEmCentavos, "BRL"),
                PROGRAMACAO, Map.of()));
        var estoque = estoques.findById(produto.getId()).orElseThrow();
        estoque.repor(quantidade);
        estoques.save(estoque);
        return produto.getId();
    }

    @Nested
    class Paginacao {

        // Sete pedidos, um por dia; o 3 e o 4 foram criados no mesmo instante, e quem desempata é o id.
        // Na ordem da listagem (mais recentes primeiro): 7, 6, 5, 4, 3, 2, 1.
        @BeforeEach
        void criarPedidos() {
            for (var n = 1; n <= 7; n++) {
                inserirPedido(n, n == 3 ? "2026-10-04T10:00:00Z" : "2026-10-0" + n + "T10:00:00Z");
            }
        }

        @Test
        void percorreTodasAsPaginasPeloNextSemRepetirNemPular() {
            var vistos = new ArrayList<UUID>();
            URI pagina = URI.create("/v1/pedidos?clienteId=" + clienteId + "&limit=3");
            var paginas = 0;
            while (pagina != null) {
                var resposta = mvc.get().uri(pagina).exchange();
                assertThat(resposta).hasStatusOk();
                vistos.addAll(ids(resposta));
                pagina = link(resposta, "next");
                paginas++;
            }

            assertThat(paginas).isEqualTo(3);
            assertThat(vistos).containsExactly(id(7), id(6), id(5), id(4), id(3), id(2), id(1));
        }

        @Test
        void umPedidoNovoEntreDuasPaginasNaoRepeteNemPulaNenhum() {
            var primeira = mvc.get().uri("/v1/pedidos?clienteId={id}&limit=3", clienteId).exchange();
            assertThat(ids(primeira)).containsExactly(id(7), id(6), id(5));

            // Com offset, o 5 apareceria de novo no começo da página 2 (13.03).
            inserirPedido(8, "2026-10-08T10:00:00Z");
            var segunda = mvc.get().uri(link(primeira, "next")).exchange();

            assertThat(ids(segunda)).containsExactly(id(4), id(3), id(2));
        }

        @Test
        void voltaPeloPrevEAPrimeiraPaginaNaoTemPrev() {
            var primeira = mvc.get().uri("/v1/pedidos?clienteId={id}&limit=3", clienteId).exchange();
            var segunda = mvc.get().uri(link(primeira, "next")).exchange();
            var deVolta = mvc.get().uri(link(segunda, "prev")).exchange();

            assertThat(ids(deVolta)).containsExactly(id(7), id(6), id(5));
            assertThat(link(deVolta, "prev")).isNull();
            assertThat(link(deVolta, "next")).isNotNull();
            assertThat(link(primeira, "prev")).isNull();
        }

        @Test
        void aUltimaPaginaNaoTemNextEOFirstRecomecaSemCursor() {
            var resposta = mvc.get().uri("/v1/pedidos?clienteId={id}&limit=10", clienteId).exchange();

            assertThat(ids(resposta)).hasSize(7);
            assertThat(link(resposta, "next")).isNull();
            assertThat(link(resposta, "first")).hasToString(
                    "http://localhost/v1/pedidos?clienteId=" + clienteId + "&limit=10");
        }

        @Test
        void recusaUmCursorAlteradoOuDeOutroCliente() {
            var primeira = mvc.get().uri("/v1/pedidos?clienteId={id}&limit=3", clienteId).exchange();
            var cursor = UriComponentsBuilder.fromUri(link(primeira, "next")).build().getQueryParams().getFirst("cursor");

            assertThat(mvc.get().uri("/v1/pedidos?clienteId={id}&cursor=abc", clienteId).exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().extractingPath("$.type").isEqualTo("https://api.bazar.example/problemas/cursor-invalido");
            // O cursor é um JSON legível em Base64; trocar um campo por um nome que o formato não tem também é recusado.
            var alterado = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8)
                            .replace("\"id\"", "\"numero\"").getBytes(StandardCharsets.UTF_8));
            assertThat(mvc.get().uri("/v1/pedidos?clienteId={id}&cursor={c}", clienteId, alterado).exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(mvc.get().uri("/v1/pedidos?clienteId={id}&cursor={c}", UUID.randomUUID(), cursor).exchange())
                    .hasStatus(HttpStatus.BAD_REQUEST)
                    .bodyJson().extractingPath("$.type").isEqualTo("https://api.bazar.example/problemas/cursor-invalido");
        }

        // Com "criado_em < ? or (criado_em = ? and id < ?)", que é o que o Window do Spring Data gera, o PostgreSQL
        // não limita a busca no índice: lê e descarta cada pedido antes da posição, como um offset.
        @Test
        void comparaAPosicaoComoUmaLinhaParaOIndiceComecarNela(CapturedOutput saida) {
            var primeira = mvc.get().uri("/v1/pedidos?clienteId={id}&limit=3", clienteId).exchange();
            logs.setLogLevel("org.hibernate.SQL", LogLevel.DEBUG);
            try {
                mvc.get().uri(link(primeira, "next")).exchange();
            } finally {
                logs.setLogLevel("org.hibernate.SQL", null);
            }

            assertThat(saida).contains(
                    "where p1_0.cliente_id=? and (p1_0.criado_em,p1_0.id)<(?,?) order by p1_0.criado_em desc,p1_0.id desc");
        }

        private void inserirPedido(int n, String criadoEm) {
            jdbc.sql("""
                    insert into pedido (id, cliente_id, status, moeda, valor_itens, valor_frete, valor_total, versao,
                                        criado_em)
                    values (?, ?, 'CRIADO', 'BRL', 10, 0, 10, 0, ?)
                    """)
                    .params(id(n), clienteId, Timestamp.from(Instant.parse(criadoEm)))
                    .update();
        }

        // Ids fixos e crescentes: 00000000-0000-0000-0000-00000000000n.
        private static UUID id(int n) {
            return new UUID(0, n);
        }

        private static List<UUID> ids(MvcTestResult resposta) {
            List<String> ids = JsonPath.read(new String(resposta.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8),
                    "$.itens[*].id");
            return ids.stream().map(UUID::fromString).toList();
        }

        private static @Nullable URI link(MvcTestResult resposta, String relacao) {
            var header = resposta.getResponse().getHeader(HttpHeaders.LINK);
            var encontrado = Pattern.compile("<([^>]*)>; rel=\"" + relacao + "\"").matcher(header == null ? "" : header);
            return encontrado.find() ? URI.create(encontrado.group(1)) : null;
        }
    }
}
