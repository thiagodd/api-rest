package com.bazar.compartilhado.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Compressão (22.03). A das respostas é feita pelo Tomcat, e só existe pela rede: o MockMvc não passa por ele.
 */
@TesteDeIntegracao
class CompressaoTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    @LocalServerPort
    int porta;

    @Autowired
    ProdutoService produtos;

    @Autowired
    JdbcClient jdbc;

    final HttpClient http = HttpClient.newHttpClient();
    UUID cleanCode;

    @BeforeEach
    void criarProdutos() {
        for (int i = 1; i <= 30; i++) {
            var id = produtos.criar(new Produto("LIV-%04d".formatted(i), "Livro de programação número " + i, null,
                    Dinheiro.deCentavos(9990 + i, "BRL"), PROGRAMACAO, Map.of())).getId();
            if (i == 1) {
                cleanCode = id;
            }
        }
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void umaListaGrandeVemEmGzipParaQuemPede() throws Exception {
        var semGzip = get("/v1/produtos?limit=30", Map.of());
        var comGzip = get("/v1/produtos?limit=30", Map.of("Accept-Encoding", "gzip"));

        assertThat(semGzip.headers().firstValue("Content-Encoding")).isEmpty();
        assertThat(comGzip.headers().firstValue("Content-Encoding")).hasValue("gzip");
        assertThat(comGzip.headers().allValues("Vary")).anyMatch(vary -> vary.contains("accept-encoding"));
        assertThat(comGzip.body().length).isLessThan(semGzip.body().length / 3);
        assertThat(descomprimir(comGzip.body())).isEqualTo(semGzip.body());
    }

    @Test
    void soGzipEhOferecido() throws Exception {
        assertThat(get("/v1/produtos?limit=30", Map.of("Accept-Encoding", "br, zstd")).headers()
                .firstValue("Content-Encoding")).isEmpty();
    }

    @Test
    void umaRespostaComEtagForteNaoEhComprimida() throws Exception {
        // O ETag forte promete os mesmos bytes; o Tomcat prefere não comprimir a quebrar a promessa (20.02).
        var detalhe = get("/v2/produtos/" + cleanCode, Map.of("Accept-Encoding", "gzip"));
        assertThat(detalhe.headers().firstValue("ETag")).isPresent();
        assertThat(detalhe.headers().firstValue("Content-Encoding")).isEmpty();
    }

    @Test
    void umCorpoEmGzipEhAceito() throws Exception {
        var json = """
                { "sku": "LIV-0101", "nome": "Domain-Driven Design", "precoEmCentavos": 21990,
                  "categoriaId": "%s" }""".formatted(PROGRAMACAO);

        var resposta = post("/v1/produtos", comprimir(json.getBytes(StandardCharsets.UTF_8)), "gzip");

        assertThat(resposta.statusCode()).isEqualTo(201);
    }

    @Test
    void umaBombaDeDescompressaoEh413() throws Exception {
        // 11 MB de espaços entre os campos de um JSON, comprimidos para uns 11 KB. Fora de uma string: dentro, o
        // limite de tamanho de string do Jackson (23.02) chegaria antes.
        var bomba = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(bomba)) {
            gzip.write("{ \"sku\": \"LIV-0102\", ".getBytes(StandardCharsets.UTF_8));
            var espacos = " ".repeat(1024 * 1024).getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < 11; i++) {
                gzip.write(espacos);
            }
            gzip.write("\"nome\": \"x\" }".getBytes(StandardCharsets.UTF_8));
        }

        var resposta = post("/v1/produtos", bomba.toByteArray(), "gzip");

        // Desde a 23.02, o limite do JSON (1 MB, contado depois da descompressão) chega antes do limite da
        // descompressão (10 MB). As duas defesas continuam: a segunda vale para corpos que não são JSON.
        assertThat(resposta.statusCode()).isEqualTo(413);
        assertThat(new String(resposta.body(), StandardCharsets.UTF_8))
                .contains("\"codigo\":\"CORPO_GRANDE_DEMAIS\"")
                .contains("passa do limite de 1 MB");
    }

    @Test
    void umaCodificacaoDesconhecidaEh415ComOAcceptEncoding() throws Exception {
        var resposta = post("/v1/produtos", "{}".getBytes(StandardCharsets.UTF_8), "br");

        assertThat(resposta.statusCode()).isEqualTo(415);
        assertThat(resposta.headers().firstValue("Accept-Encoding")).hasValue("gzip");
        assertThat(new String(resposta.body(), StandardCharsets.UTF_8))
                .contains("\"codigo\":\"CODIFICACAO_NAO_SUPORTADA\"")
                .contains("O corpo veio com Content-Encoding br");
    }

    private HttpResponse<byte[]> get(String caminho, Map<String, String> cabecalhos) throws Exception {
        var requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
        cabecalhos.forEach(requisicao::header);
        return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpResponse<byte[]> post(String caminho, byte[] corpo, String codificacao) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .header("Content-Type", "application/json")
                .header("Content-Encoding", codificacao)
                .header("Accept-Language", "pt-BR")
                .POST(HttpRequest.BodyPublishers.ofByteArray(corpo))
                .build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private static byte[] comprimir(byte[] dados) throws IOException {
        var saida = new ByteArrayOutputStream();
        try (var gzip = new GZIPOutputStream(saida)) {
            gzip.write(dados);
        }
        return saida.toByteArray();
    }

    private static byte[] descomprimir(byte[] dados) throws IOException {
        try (var gzip = new GZIPInputStream(new java.io.ByteArrayInputStream(dados))) {
            return gzip.readAllBytes();
        }
    }
}
