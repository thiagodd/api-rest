package com.bazar.catalogo;

import java.util.Map;
import java.util.UUID;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A exportação do catálogo (22.02). O MockMvcTester espera a resposta assíncrona do StreamingResponseBody.
 */
@TesteDeIntegracao
class ExportacaoDeProdutosTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ProdutoService produtos;

    @Autowired
    JdbcClient jdbc;

    UUID cleanCode;

    @BeforeEach
    void criarProdutos() {
        cleanCode = criar("LIV-0042", "Clean Code", 12990);
        criar("LIV-0077", "Refactoring, 2nd ed.", 18900);
        criar("LIV-0099", "=HYPERLINK(\"https://evil.example\",\"Clique\")", 1000);
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void emCsvComCabecalhoEscapeEProtecaoContraFormulas() throws Exception {
        var resposta = mvc.get().uri("/v2/produtos/exportacao").header("Accept", "text/csv").exchange();

        assertThat(resposta).hasStatusOk()
                .hasHeader("Content-Disposition", "attachment; filename=\"catalogo-" + java.time.LocalDate.now()
                        + ".csv\"")
                .hasHeader("Cache-Control", "no-store")
                .doesNotContainHeader("Content-Length");
        var linhas = resposta.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8).split("\n");
        assertThat(linhas).hasSize(4);
        assertThat(linhas[0]).isEqualTo("id,sku,nome,status,precoValor,precoMoeda,categoriaId,criadoEm");
        assertThat(linhas[1]).startsWith(cleanCode + ",LIV-0042,Clean Code,RASCUNHO,129.90,BRL,");
        assertThat(linhas[2]).contains(",\"Refactoring, 2nd ed.\",");
        // A fórmula vira texto: apóstrofo na frente, e aspas dobradas por causa das aspas e da vírgula.
        assertThat(linhas[3]).contains(",\"'=HYPERLINK(\"\"https://evil.example\"\",\"\"Clique\"\")\",");
    }

    @Test
    void emNdjsonUmObjetoPorLinha() throws Exception {
        var resposta = mvc.get().uri("/v2/produtos/exportacao").header("Accept", "application/x-ndjson").exchange();

        assertThat(resposta).hasStatusOk().hasHeader("Vary", "Accept");
        var linhas = resposta.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8).split("\n");
        assertThat(linhas).hasSize(3);
        assertThat(linhas[0]).startsWith("{\"id\":\"" + cleanCode + "\",\"sku\":\"LIV-0042\",\"nome\":\"Clean Code\"");
    }

    private UUID criar(String sku, String nome, long centavos) {
        return produtos.criar(new Produto(sku, nome, null, Dinheiro.deCentavos(centavos, "BRL"), PROGRAMACAO,
                Map.of())).getId();
    }
}
