package com.bazar.catalogo.api;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.web.ListaCsvHttpMessageConverter;
import com.jayway.jsonpath.JsonPath;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

// A paginação por cursor dos produtos (13.04), com o banco de verdade: o que importa é a condição no SQL.
@TesteDeIntegracao
@ExtendWith(OutputCaptureExtension.class)
class PaginacaoDeProdutosTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ProdutoService produtos;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    LoggingSystem logs;

    // Sete produtos, com preços repetidos: em sort=precoEmCentavos, quem desempata é o id.
    @BeforeEach
    void criarProdutos() {
        var precos = List.of(4990, 12990, 4990, 8990, 12990, 4990, 15900);
        for (var n = 0; n < precos.size(); n++) {
            produtos.criar(new Produto("LIV-00%02d".formatted(n + 10), "Livro " + (char) ('G' - n), null,
                    Dinheiro.deCentavos(precos.get(n), "BRL"), PROGRAMACAO, Map.of()));
        }
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "&sort=precoEmCentavos,desc", "&sort=sku", "&sort=criadoEm,desc",
            "&sort=precoEmCentavos&sort=nome,desc" })
    void percorreTodasAsPaginasNasDuasDirecoesEmQualquerOrdenacao(String ordenacao) {
        var todos = ids(mvc.get().uri(URI.create("/v1/produtos?limit=100" + ordenacao)).exchange());

        // Para a frente, pelo next, até o fim.
        var paraFrente = new ArrayList<UUID>();
        MvcTestResult ultima = null;
        for (var pagina = URI.create("/v1/produtos?limit=2" + ordenacao); pagina != null; pagina = link(ultima, "next")) {
            ultima = mvc.get().uri(pagina).exchange();
            assertThat(ultima).hasStatusOk();
            paraFrente.addAll(ids(ultima));
        }
        // E de volta, pelo prev, até o começo.
        var paraTras = new ArrayList<>(ids(ultima));
        for (var pagina = link(ultima, "prev"); pagina != null; pagina = link(ultima, "prev")) {
            ultima = mvc.get().uri(pagina).exchange();
            paraTras.addAll(0, ids(ultima));
        }

        assertThat(todos).hasSize(7);
        assertThat(paraFrente).isEqualTo(todos);
        assertThat(paraTras).isEqualTo(todos);
    }

    @Test
    void umProdutoNovoEntreDuasPaginasNaoRepeteNemPulaNenhum() {
        var primeira = mvc.get().uri("/v1/produtos?limit=3").exchange();
        assertThat(nomes(primeira)).containsExactly("Livro A", "Livro B", "Livro C");

        produtos.criar(new Produto("LIV-0099", "Livro 0", null, Dinheiro.deCentavos(990, "BRL"), PROGRAMACAO,
                Map.of()));

        assertThat(nomes(mvc.get().uri(link(primeira, "next")).exchange()))
                .containsExactly("Livro D", "Livro E", "Livro F");
    }

    @Test
    void umCursorSoServeNaConsultaEmQueFoiEmitido() {
        var primeira = mvc.get().uri("/v1/produtos?limit=3&sort=precoEmCentavos").exchange();
        var cursor = link(primeira, "next").getRawQuery().replaceAll(".*cursor=", "");

        // Os valores do cursor são preços: na ordem por nome, não querem dizer nada.
        assertThat(mvc.get().uri("/v1/produtos?limit=3&sort=nome&cursor={c}", cursor).exchange())
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.type").isEqualTo("https://api.bazar.example/problemas/cursor-invalido");
        assertThat(mvc.get().uri("/v1/produtos?limit=3&sort=precoEmCentavos&status=ATIVO&cursor={c}", cursor).exchange())
                .hasStatus(HttpStatus.BAD_REQUEST);
        // O limit pode mudar de uma página para outra.
        assertThat(mvc.get().uri("/v1/produtos?limit=5&sort=precoEmCentavos&cursor={c}", cursor).exchange())
                .hasStatusOk();
    }

    @Test
    void aBuscaComCorpoEOCsvTambemPaginam() {
        var corpo = """
                { "categoriaIds": [ "%s" ] }
                """.formatted(PROGRAMACAO);
        var primeira = mvc.post().uri("/v1/produtos/buscas?limit=4").contentType(MediaType.APPLICATION_JSON)
                .content(corpo).exchange();
        var segunda = mvc.post().uri(link(primeira, "next")).contentType(MediaType.APPLICATION_JSON)
                .content(corpo).exchange();
        assertThat(nomes(segunda)).containsExactly("Livro E", "Livro F", "Livro G");

        // No CSV, que não tem onde guardar um link, a navegação vai no header.
        var csv = mvc.get().uri("/v1/produtos?limit=4").accept(ListaCsvHttpMessageConverter.TEXT_CSV).exchange();
        assertThat(link(csv, "next")).isNotNull();
    }

    // O primeiro termo é redundante, mas é o limite que o índice (nome, id) usa para começar na posição.
    @Test
    void aCondicaoComecaPorUmLimiteNoPrimeiroCampo(CapturedOutput saida) {
        var primeira = mvc.get().uri("/v1/produtos?limit=3").exchange();
        logs.setLogLevel("org.hibernate.SQL", LogLevel.DEBUG);
        try {
            mvc.get().uri(link(primeira, "next")).exchange();
        } finally {
            logs.setLogLevel("org.hibernate.SQL", null);
        }

        assertThat(saida).contains("where p1_0.nome>=? and (p1_0.nome>? or p1_0.nome=? and p1_0.id>?) order by 3,1");
    }

    private static List<UUID> ids(MvcTestResult resposta) {
        List<String> ids = JsonPath.read(corpo(resposta), "$.itens[*].id");
        return ids.stream().map(UUID::fromString).toList();
    }

    private static List<String> nomes(MvcTestResult resposta) {
        return JsonPath.read(corpo(resposta), "$.itens[*].nome");
    }

    private static String corpo(MvcTestResult resposta) {
        return new String(resposta.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private static @Nullable URI link(MvcTestResult resposta, String relacao) {
        var header = resposta.getResponse().getHeader(HttpHeaders.LINK);
        var encontrado = Pattern.compile("<([^>]*)>; rel=\"" + relacao + "\"").matcher(header == null ? "" : header);
        return encontrado.find() ? URI.create(encontrado.group(1)) : null;
    }
}
