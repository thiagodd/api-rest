package com.bazar.relatorios.infraestrutura;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bazar.LimpezaDoBanco;
import com.bazar.TestcontainersConfiguration;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.estoque.dominio.EstoqueRepository;
import com.bazar.pedidos.dominio.ItemSolicitado;
import com.bazar.pedidos.dominio.PedidoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ConsultaDeVendasTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    @Autowired
    ConsultaDeVendas consulta;

    @Autowired
    PedidoService pedidos;

    @Autowired
    ProdutoService produtos;

    @Autowired
    EstoqueRepository estoques;

    @Autowired
    ClienteRepository clientes;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void somaAsVendasPorProdutoNoPeriodoSemContarCancelados() {
        var cliente = clientes.save(Cliente.novo("Ana Souza", "ana@example.com", "52998224725", null, Canal.WEB)).id();
        var cleanCode = criarProdutoComEstoque("LIV-0042", "Clean Code", 12990);
        var refactoring = criarProdutoComEstoque("LIV-0043", "Refactoring", 15900);

        pedidos.criar(cliente, List.of(new ItemSolicitado(cleanCode, 2), new ItemSolicitado(refactoring, 1)));
        pedidos.criar(cliente, List.of(new ItemSolicitado(cleanCode, 1)));
        var cancelado = pedidos.criar(cliente, List.of(new ItemSolicitado(refactoring, 5)));
        // O cancelamento pela API chega em 12.04.
        jdbc.sql("update pedido set status = 'CANCELADO' where id = ?").param(cancelado.getId()).update();

        var agora = Instant.now();
        var vendas = consulta.porProduto(agora.minus(1, ChronoUnit.HOURS), agora.plus(1, ChronoUnit.HOURS));

        assertThat(vendas).containsExactly(
                new VendasDoProduto(cleanCode, "LIV-0042", "Clean Code", 2, 3, new BigDecimal("389.70")),
                new VendasDoProduto(refactoring, "LIV-0043", "Refactoring", 1, 1, new BigDecimal("159.00")));
        assertThat(consulta.porProduto(agora.plus(1, ChronoUnit.HOURS), agora.plus(2, ChronoUnit.HOURS))).isEmpty();
    }

    private UUID criarProdutoComEstoque(String sku, String nome, long precoEmCentavos) {
        var produto = produtos.criar(new Produto(sku, nome, null, Dinheiro.deCentavos(precoEmCentavos, "BRL"),
                PROGRAMACAO, Map.of()));
        var estoque = estoques.findById(produto.getId()).orElseThrow();
        estoque.repor(10);
        estoques.save(estoque);
        return produto.getId();
    }
}
