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
import org.springframework.data.domain.Sort;
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

    // A ordenação já traduzida pela API: propriedades da entidade e o desempate pelo id (13.02).
    static final Sort POR_NOME = Sort.by("nome", "id");

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
        assertThat(servico.listar(FiltroDeProdutos.nenhum(), POR_NOME))
                .extracting(ProdutoResumo::sku)
                .containsExactly("ELE-0001", "LIV-0042", "LIV-0043");
        assertThat(saida).contains(
                "select p1_0.id,p1_0.sku,p1_0.nome,p1_0.preco_valor,p1_0.preco_moeda,p1_0.status,p1_0.categoria_id from produto p1_0");
    }

    @Test
    void combinaSoOsFiltrosInformados() {
        assertThat(servico.listar(FiltroDeProdutos.nenhum().daCategoria(PROGRAMACAO), POR_NOME))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0042", "LIV-0043");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().daCategoria(PROGRAMACAO).comStatus(StatusProduto.RASCUNHO)
                .custando(new BigDecimal("130.00"), null), POR_NOME))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0043");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().custando(null, new BigDecimal("129.90")).comTexto("c"), POR_NOME))
                .extracting(ProdutoResumo::sku).containsExactly("ELE-0001", "LIV-0042");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comStatus(StatusProduto.ATIVO), POR_NOME)).isEmpty();
    }

    @Test
    void trataOsCuringasDoTextoComoTextoComum() {
        // Sem o escape, "%" casaria com qualquer nome.
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comTexto("100%"), POR_NOME))
                .extracting(ProdutoResumo::sku).containsExactly("ELE-0001");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comTexto("%"), POR_NOME)).hasSize(1);
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comTexto("_"), POR_NOME)).isEmpty();
    }

    @Test
    void filtraPorAtributoNoJsonbComChaveEValorComoParametros(CapturedOutput saida) {
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comAtributo("autor", "Robert C. Martin"), POR_NOME))
                .extracting(ProdutoResumo::sku).containsExactly("LIV-0042");
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comAtributo("autor", "Martin Fowler"), POR_NOME)).isEmpty();
        // Os valores vão como parâmetros: uma aspa no valor não quebra a consulta, só não casa com nada.
        assertThat(servico.listar(FiltroDeProdutos.nenhum().comAtributo("autor'", "x' or '1'='1"), POR_NOME)).isEmpty();
        assertThat(saida).contains("jsonb_extract_path_text(p1_0.atributos,?)=?").doesNotContain("'1'='1");
    }

    @Test
    void umaListaDeCategoriasEhUmOu() {
        assertThat(servico.listar(FiltroDeProdutos.nenhum().daCategoria(PROGRAMACAO, ELETRONICOS), POR_NOME)).hasSize(3);
    }

    @Test
    void filtraPorSkuEPorDataDeCriacao() {
        var antes = Instant.now().minusSeconds(3600);
        var depois = Instant.now().plusSeconds(3600);
        var porSku = new FiltroDeProdutos(null, "LIV-0043", Set.of(), Set.of(), null, null, null, Map.of());
        var criadosAposAntes = new FiltroDeProdutos(null, null, Set.of(), Set.of(), null, null, antes, Map.of());
        var criadosAposDepois = new FiltroDeProdutos(null, null, Set.of(), Set.of(), null, null, depois, Map.of());

        assertThat(servico.listar(porSku, POR_NOME)).extracting(ProdutoResumo::nome).containsExactly("Refactoring");
        assertThat(servico.listar(criadosAposAntes, POR_NOME)).hasSize(3);
        assertThat(servico.listar(criadosAposDepois, POR_NOME)).isEmpty();
    }

    @Test
    void ordenaPelosCamposPedidosEDesempataPeloId(CapturedOutput saida) {
        servico.criar(new Produto("LIV-0044", "Domain-Driven Design", null, Dinheiro.deCentavos(15900, "BRL"), PROGRAMACAO,
                Map.of()));
        servico.criar(new Produto("LIV-0045", "Effective Java", null, Dinheiro.deCentavos(15900, "BRL"), PROGRAMACAO,
                Map.of()));

        var porPreco = servico.listar(FiltroDeProdutos.nenhum(), Sort.by(Sort.Order.desc("preco.valor"), Sort.Order.asc("id")));

        assertThat(porPreco).extracting(ProdutoResumo::sku).endsWith("LIV-0042", "ELE-0001");
        // Três produtos custam R$ 159,00: entre eles, quem decide é o id. Os ids ainda são aleatórios (v4; o v7 chega
        // em 15.03), então a ordem não é a de criação, mas é sempre a mesma. O PostgreSQL compara uuid byte a byte,
        // como o texto em hexadecimal; UUID.compareTo, do Java, compara com sinal e discordaria dele.
        var empatados = porPreco.subList(0, 3);
        assertThat(empatados).extracting(ProdutoResumo::sku).containsExactlyInAnyOrder("LIV-0043", "LIV-0044", "LIV-0045");
        assertThat(empatados).extracting(produto -> produto.id().toString()).isSorted();
        // O Hibernate cita o id pela posição na lista do select (1), como o nome em "order by 3" (10.05).
        assertThat(saida).contains("order by p1_0.preco_valor desc,1");
    }
}
