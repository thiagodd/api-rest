package com.bazar.catalogo;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.catalogo.dominio.ArmazenamentoDeArquivos;
import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.Dinheiro;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Upload de imagens (22.01). O MockMvc monta o multipart já separado em partes, sem passar pelo Tomcat; o limite de
 * tamanho do multipart só é testado pela rede.
 */
@TesteDeIntegracao
class ImagensDeProdutoTest {

    static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");

    @Autowired
    MockMvcTester mvc;

    @LocalServerPort
    int porta;

    @Autowired
    ProdutoService produtos;

    @Autowired
    ArmazenamentoDeArquivos armazenamento;

    @Autowired
    JdbcClient jdbc;

    UUID cleanCode;

    @BeforeEach
    void criarOProduto() {
        cleanCode = produtos.criar(new Produto("LIV-0042", "Clean Code", null, Dinheiro.deCentavos(12990, "BRL"),
                PROGRAMACAO, Map.of())).getId();
    }

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void umPngComMetadadosEhGravadoPeloQueEle() throws Exception {
        // O cliente diz que é um JPEG chamado capa.jpg; o conteúdo é um PNG.
        var arquivo = new MockMultipartFile("arquivo", "../../capa.jpg", "image/jpeg", imagem("png", 640, 480));
        var metadados = new MockMultipartFile("metadados", "", "application/json",
                "{ \"textoAlternativo\": \"Capa do livro\" }".getBytes(StandardCharsets.UTF_8));

        var resposta = mvc.post().uri("/v1/produtos/{id}/imagens", cleanCode).multipart().file(arquivo).file(metadados)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED).bodyJson().isLenientlyEqualTo("""
                { "nomeOriginal": "capa.jpg", "tipoConteudo": "image/png", "largura": 640, "altura": 480,
                  "textoAlternativo": "Capa do livro", "ordem": 1 }
                """);
        assertThat(resposta.getResponse().getHeader("Location"))
                .matches("http://localhost/v1/produtos/" + cleanCode + "/imagens/[0-9a-f-]{36}");
        var chave = jdbc.sql("select chave from imagem_produto").query(String.class).single();
        assertThat(chave).endsWith(".png");
        assertThat(Files.size(armazenamento.caminho(chave))).isEqualTo(arquivo.getSize());
    }

    @Test
    void umArquivoQueNaoEhImagemEh415SejaQualForOContentType() {
        var script = new MockMultipartFile("arquivo", "foto.jpg", "image/jpeg",
                "<?php system($_GET['c']); ?>".getBytes(StandardCharsets.UTF_8));

        assertThat(mvc.post().uri("/v1/produtos/{id}/imagens", cleanCode).multipart().file(script))
                .hasStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .bodyJson().extractingPath("$.codigo").isEqualTo("IMAGEM_NAO_ACEITA");
        assertThat(jdbc.sql("select count(*) from imagem_produto").query(Long.class).single()).isZero();
    }

    @Test
    void umPngQueDeclaraDimensoesEnormesEh422ENaoDeixaArquivo() throws Exception {
        var bomba = comDimensoes(imagem("png", 10, 10), 50_000, 50_000);

        assertThat(mvc.post().uri("/v1/produtos/{id}/imagens", cleanCode).multipart()
                .file(new MockMultipartFile("arquivo", "bomba.png", "image/png", bomba)))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.erros[0].campo").isEqualTo("arquivo");
        try (var arquivos = Files.list(armazenamento.caminho("x").getParent())) {
            assertThat(arquivos.filter(arquivo -> arquivo.getFileName().toString().startsWith("upload-"))).isEmpty();
        }
    }

    @Test
    void oArquivoTambemPodeSerOCorpoDaRequisicao() throws Exception {
        assertThat(mvc.post().uri("/v1/produtos/{id}/imagens", cleanCode).contentType(MediaType.IMAGE_JPEG)
                .header("Content-Disposition", "attachment; filename=\"capa.jpg\"")
                .content(imagem("jpg", 300, 400)))
                .hasStatus(HttpStatus.CREATED).bodyJson().isLenientlyEqualTo("""
                        { "nomeOriginal": "capa.jpg", "tipoConteudo": "image/jpeg", "largura": 300, "altura": 400 }
                        """);
    }

    @Test
    void umCorpoBinarioAcimaDoLimiteEh413() {
        var grande = new byte[6 * 1024 * 1024];
        System.arraycopy(new byte[] { (byte) 0xff, (byte) 0xd8, (byte) 0xff }, 0, grande, 0, 3);

        assertThat(mvc.post().uri("/v1/produtos/{id}/imagens", cleanCode).contentType(MediaType.IMAGE_JPEG)
                .content(grande))
                .hasStatus(HttpStatus.CONTENT_TOO_LARGE)
                .bodyJson().extractingPath("$.codigo").isEqualTo("ARQUIVO_GRANDE_DEMAIS");
    }

    // Ao responder 413, o Tomcat fecha a conexão sem ler o resto do corpo. Um cliente que só lê a resposta depois de
    // enviar tudo (o HttpClient do JDK, o RestTestClient) recebe um "broken pipe"; o curl e os navegadores leem
    // enquanto enviam. O teste faz como eles, num socket: uma thread envia, a outra lê.
    @Test
    void umMultipartAcimaDoLimiteEh413NoTomcat() throws Exception {
        var delimitador = "----bazar";
        var inicio = ("--" + delimitador + "\r\nContent-Disposition: form-data; name=\"arquivo\"; filename=\"grande.jpg\""
                + "\r\nContent-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.US_ASCII);
        var fim = ("\r\n--" + delimitador + "--\r\n").getBytes(StandardCharsets.US_ASCII);
        var arquivo = new byte[20 * 1024 * 1024];
        try (var socket = new Socket("localhost", porta)) {
            Thread.ofVirtual().start(() -> {
                try {
                    var saida = socket.getOutputStream();
                    saida.write(("POST /v1/produtos/" + cleanCode + "/imagens HTTP/1.1\r\nHost: localhost\r\n"
                            + "Accept-Language: en\r\nContent-Type: multipart/form-data; boundary=" + delimitador
                            + "\r\nContent-Length: " + (inicio.length + arquivo.length + fim.length) + "\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
                    saida.write(inicio);
                    saida.write(arquivo);
                    saida.write(fim);
                }
                catch (IOException ex) {
                    // O servidor fechou a conexão no meio do envio: esperado.
                }
            });
            var resposta = new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            assertThat(resposta).startsWith("HTTP/1.1 413").contains("Connection: close")
                    .contains("\"codigo\":\"ARQUIVO_GRANDE_DEMAIS\"")
                    .contains("\"detail\":\"The file exceeds the 5 MB limit.\"");
        }
    }

    @Test
    void umProdutoTemNoMaximoDezImagens() throws Exception {
        var png = imagem("png", 8, 8);
        for (int i = 0; i < 10; i++) {
            assertThat(mvc.post().uri("/v1/produtos/{id}/imagens", cleanCode).contentType(MediaType.IMAGE_PNG)
                    .content(png)).hasStatus(HttpStatus.CREATED);
        }
        assertThat(mvc.post().uri("/v1/produtos/{id}/imagens", cleanCode).contentType(MediaType.IMAGE_PNG)
                .content(png)).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.codigo").isEqualTo("LIMITE_DE_IMAGENS");
    }

    static byte[] imagem(String formato, int largura, int altura) throws IOException {
        var saida = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB), formato, saida);
        return saida.toByteArray();
    }

    // Reescreve largura e altura no cabeçalho IHDR do PNG e recalcula o CRC: um arquivo pequeno que declara uma
    // imagem enorme.
    static byte[] comDimensoes(byte[] png, int largura, int altura) {
        var bytes = ByteBuffer.wrap(png.clone());
        bytes.putInt(16, largura).putInt(20, altura);
        var crc = new CRC32();
        crc.update(bytes.array(), 12, 17);
        bytes.putInt(29, (int) crc.getValue());
        return bytes.array();
    }
}
