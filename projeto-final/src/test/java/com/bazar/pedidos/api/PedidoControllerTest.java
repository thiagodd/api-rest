package com.bazar.pedidos.api;

import java.util.Map;
import java.util.UUID;

import com.bazar.ContadorDeConsultas;
import com.bazar.LimpezaDoBanco;
import com.bazar.TestcontainersConfiguration;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.estoque.dominio.EstoqueRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

// As estatísticas do Hibernate permitem contar as consultas de cada requisição (10.04).
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
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

    private UUID criarProdutoComEstoque(String sku, String nome, long precoEmCentavos, int quantidade) {
        var produto = produtos.criar(new Produto(sku, nome, null, Dinheiro.deCentavos(precoEmCentavos, "BRL"),
                PROGRAMACAO, Map.of()));
        var estoque = estoques.findById(produto.getId()).orElseThrow();
        estoque.repor(quantidade);
        estoques.save(estoque);
        return produto.getId();
    }
}
