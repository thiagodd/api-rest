package com.bazar.catalogo.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
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
        assertThat(servico.listar(FiltroDeProdutos.nenhum().daCategoria(PROGRAMACAO)))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0042", "LIV-0043");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().daCategoria(PROGRAMACAO).comStatus(StatusProduto.RASCUNHO)
                .custando(new BigDecimal("130.00"), null)))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0043");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().custando(null, new BigDecimal("129.90")).comTexto("c")))
                .extracting(ProdutoResumo::sku).containsExactly("ELE-0001", "LIV-0042");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comStatus(StatusProduto.ATIVO))).isEmpty();
    }

    @Test
    void trataOsCuringasDoTextoComoTextoComum() {
        // Sem o escape, "%" casaria com qualquer nome.
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comTexto("100%")))
                .extracting(ProdutoResumo::sku).containsExactly("ELE-0001");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comTexto("%"))).hasSize(1);
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comTexto("_"))).isEmpty();
    }

    @Test
    void filtraPorAtributoNoJsonbComChaveEValorComoParametros(CapturedOutput saida) {
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comAtributo("autor", "Robert C. Martin")))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0042");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comAtributo("autor", "Martin Fowler"))).isEmpty();
        // Os valores vão como parâmetros: uma aspa no valor não quebra a consulta, só não casa com nada.
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comAtributo("autor'", "x' or '1'='1"))).isEmpty();
        assertThat(saida).contains("jsonb_extract_path_text(p1_0.atributos,?)=?").doesNotContain("'1'='1");
    }

    @Test
    void umaListaDeCategoriasEhUmOu() {
        assertThat(servico.listar(FiltroDeProdutos.nenhum().daCategoria(PROGRAMACAO, ELETRONICOS))).hasSize(3);
    }

    @Test
    void filtraPorSkuEPorDataDeCriacao() {
        var antes = Instant.now().minusSeconds(3600);
        var depois = Instant.now().plusSeconds(3600);
        var porSku = new FiltroDeProdutos(null, "LIV-0043", Set.of(), Set.of(), null, null, null, Map.of());
        var criadosAposAntes = new FiltroDeProdutos(null, null, Set.of(), Set.of(), null, null, antes, Map.of());
        var criadosAposDepois = new FiltroDeProdutos(null, null, Set.of(), Set.of(), null, null, depois, Map.of());

        assertThat(servico.listar(porSku)).extracting(ProdutoResumo::nome).containsExactly("Refactoring");
        assertThat(servico.listar(criadosAposAntes)).hasSize(3);
        assertThat(servico.listar(criadosAposDepois)).isEmpty();
    }
}
