package com.bazar.estoque.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.pedidos.dominio.ItemSolicitado;
import com.bazar.pedidos.dominio.PedidoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

// Pedidos de verdade, em paralelo, contra o PostgreSQL do Testcontainers. Cada virtual thread é uma requisição.
@TesteDeIntegracao
class ReservaConcorrenteTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

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

    UUID clienteId;

    @BeforeEach
    void criarCliente() {
        clienteId = clientes.save(Cliente.novo("Ana Souza", "ana@example.com", "52998224725", null, Canal.WEB)).id();
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void vinteComprasSimultaneasDeCincoUnidadesNaoVendemDemais() throws Exception {
        var produto = criarProdutoComEstoque("LIV-0042", 5);

        var resultados = emParalelo(20, () -> pedidos.criar(clienteId, List.of(new ItemSolicitado(produto, 1))));

        assertThat(resultados).containsExactlyInAnyOrderEntriesOf(Map.of("criado", 5, "EstoqueInsuficienteException", 15));
        assertThat(estoques.findById(produto)).get()
                .returns(0, Estoque::getQuantidadeDisponivel)
                .returns(5, Estoque::getQuantidadeReservada);
        assertThat(jdbc.sql("select count(*) from pedido").query(Long.class).single()).isEqualTo(5);
    }

    @Test
    void pedidosComOsMesmosProdutosEmOrdemInversaNaoEntramEmDeadlock() throws Exception {
        var a = criarProdutoComEstoque("LIV-0042", 100);
        var b = criarProdutoComEstoque("LIV-0043", 100);

        var resultados = emParalelo(40, indice -> indice % 2 == 0
                ? pedidos.criar(clienteId, List.of(new ItemSolicitado(a, 1), new ItemSolicitado(b, 1)))
                : pedidos.criar(clienteId, List.of(new ItemSolicitado(b, 1), new ItemSolicitado(a, 1))));

        // Sem a ordenação das reservas, parte destes pedidos falharia com "deadlock detected".
        assertThat(resultados).containsExactlyEntriesOf(Map.of("criado", 40));
        assertThat(estoques.findById(a)).get().returns(60, Estoque::getQuantidadeDisponivel);
        assertThat(estoques.findById(b)).get().returns(60, Estoque::getQuantidadeDisponivel);
    }

    private Map<String, Integer> emParalelo(int vezes, Runnable tarefa) throws Exception {
        return emParalelo(vezes, indice -> {
            tarefa.run();
            return null;
        });
    }

    private Map<String, Integer> emParalelo(int vezes, java.util.function.IntFunction<?> tarefa) throws Exception {
        var resultados = new ConcurrentHashMap<String, Integer>();
        var largada = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futuros = new ArrayList<Future<?>>();
            for (int i = 0; i < vezes; i++) {
                var indice = i;
                futuros.add(executor.submit(() -> {
                    largada.await();
                    try {
                        tarefa.apply(indice);
                        resultados.merge("criado", 1, Integer::sum);
                    } catch (RuntimeException e) {
                        resultados.merge(e.getClass().getSimpleName(), 1, Integer::sum);
                    }
                    return null;
                }));
            }
            largada.countDown();
            for (var futuro : futuros) {
                futuro.get();
            }
        }
        return resultados;
    }

    private UUID criarProdutoComEstoque(String sku, int quantidade) {
        var produto = produtos.criar(new Produto(sku, "Livro " + sku, null, Dinheiro.deCentavos(4990, "BRL"), PROGRAMACAO,
                Map.of()));
        var estoque = estoques.findById(produto.getId()).orElseThrow();
        estoque.repor(quantidade);
        estoques.save(estoque);
        return produto.getId();
    }
}
