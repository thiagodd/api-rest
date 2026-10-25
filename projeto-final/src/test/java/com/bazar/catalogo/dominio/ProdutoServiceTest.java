package com.bazar.catalogo.dominio;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.compartilhado.Dinheiro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

// O log de SQL mostra as colunas que a projeção seleciona. O nível do log muda em tempo de execução, e não com
// properties no @SpringBootTest: uma propriedade a mais criaria outro contexto e outro PostgreSQL (11.04).
@TesteDeIntegracao
@ExtendWith(OutputCaptureExtension.class)
class ProdutoServiceTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");
    static final UUID ELETRONICOS = UUID.fromString("0192f3a0-5555-7000-8000-000000000002");

    @Autowired
    ProdutoService servico;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    LoggingSystem logs;

    @BeforeEach
    void prepararCatalogo() {
        logs.setLogLevel("org.hibernate.SQL", LogLevel.DEBUG);
        servico.criar(new Produto("LIV-0042", "Clean Code", "O clássico", Dinheiro.deCentavos(12990, "BRL"), PROGRAMACAO,
                Map.of("autor", "Robert C. Martin")));
        servico.criar(new Produto("LIV-0043", "Refactoring", null, Dinheiro.deCentavos(15900, "BRL"), PROGRAMACAO, Map.of()));
        servico.criar(new Produto("ELE-0001", "Cabo USB 100% cobre", null, Dinheiro.deCentavos(2990, "BRL"), ELETRONICOS,
                Map.of()));
    }

    @AfterEach
    void limparOBanco() {
        logs.setLogLevel("org.hibernate.SQL", null);
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void listaResumosOrdenadosPorNomeSemCarregarAsEntidades(CapturedOutput saida) {
        assertThat(servico.listar(FiltroDeProdutos.nenhum()))
                .extracting(ProdutoResumo::sku)
                .containsExactly("ELE-0001", "LIV-0042", "LIV-0043");
        assertThat(saida).contains(
                "select p1_0.id,p1_0.sku,p1_0.nome,p1_0.preco_valor,p1_0.preco_moeda,p1_0.status,p1_0.categoria_id from produto p1_0");
    }

    @Test
    void combinaSoOsFiltrosInformados() {
        assertThat(servico.listar(new FiltroDeProdutos(PROGRAMACAO, null, null, null, null)))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0042", "LIV-0043");
        assertThat(servico.listar(new FiltroDeProdutos(PROGRAMACAO, StatusProduto.RASCUNHO, new BigDecimal("130.00"), null, null)))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0043");
        assertThat(servico.listar(new FiltroDeProdutos(null, null, null, new BigDecimal("129.90"), "c")))
                .extracting(ProdutoResumo::sku).containsExactly("ELE-0001", "LIV-0042");
        assertThat(servico.listar(new FiltroDeProdutos(null, StatusProduto.ATIVO, null, null, null))).isEmpty();
    }

    @Test
    void trataOsCuringasDoTextoComoTextoComum() {
        // Sem o escape, "%" casaria com qualquer nome.
        assertThat(servico.listar(new FiltroDeProdutos(null, null, null, null, "100%")))
                .extracting(ProdutoResumo::sku).containsExactly("ELE-0001");
        assertThat(servico.listar(new FiltroDeProdutos(null, null, null, null, "%"))).hasSize(1);
        assertThat(servico.listar(new FiltroDeProdutos(null, null, null, null, "_"))).isEmpty();
    }
}
