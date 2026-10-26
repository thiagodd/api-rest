package com.bazar.catalogo.api;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;

import com.bazar.catalogo.dominio.ExportacaoDeProdutos;
import com.bazar.catalogo.dominio.ExportacaoDeProdutos.LinhaDeProduto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * O catálogo inteiro num arquivo só (22.02), em CSV ou NDJSON. A resposta é escrita à medida que as linhas saem do
 * banco: a memória usada não depende do tamanho do catálogo.
 * <p>
 * Só na v2, como as imagens: a v1 de produtos está em depreciação (18.04).
 */
@RestController
@Tag(name = "Produtos")
public class ExportacaoDeProdutosController {

    static final MediaType CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);
    static final MediaType NDJSON = MediaType.parseMediaType("application/x-ndjson");
    private static final String CABECALHO_CSV = "id,sku,nome,status,precoValor,precoMoeda,categoriaId,criadoEm\n";

    private final ExportacaoDeProdutos exportacao;
    private final JsonMapper json;
    private final Clock relogio;

    public ExportacaoDeProdutosController(ExportacaoDeProdutos exportacao, JsonMapper json, Clock relogio) {
        this.exportacao = exportacao;
        this.json = json;
        this.relogio = relogio;
    }

    @GetMapping(path = "/v{versao}/produtos/exportacao", version = "2", produces = "text/csv")
    @Operation(operationId = "exportarProdutos", summary = "Exporta o catálogo inteiro",
            description = "Todos os produtos, em ordem de SKU, sem paginação: `text/csv` (com cabeçalho) ou "
                    + "`application/x-ndjson` (um objeto JSON por linha). A resposta chega aos poucos, sem "
                    + "`Content-Length`.")
    @ApiResponse(responseCode = "200", description = "O catálogo", content = {
        @Content(mediaType = "text/csv", schema = @Schema(type = "string")),
        @Content(mediaType = "application/x-ndjson", schema = @Schema(implementation = LinhaDeProduto.class)) })
    public ResponseEntity<StreamingResponseBody> exportarCsv() {
        return resposta(CSV, "csv", saida -> {
            saida.write(CABECALHO_CSV);
            exportacao.percorrer(linha -> escrever(saida, String.join(",", Csv.campo(linha.id().toString()),
                    Csv.campo(linha.sku()), Csv.campo(linha.nome()), Csv.campo(linha.status()),
                    linha.precoValor().toPlainString(), Csv.campo(linha.precoMoeda()),
                    linha.categoriaId().toString(), linha.criadoEm().toInstant().toString()) + "\n"));
        });
    }

    @GetMapping(path = "/v{versao}/produtos/exportacao", version = "2", produces = "application/x-ndjson")
    @Operation(hidden = true)
    public ResponseEntity<StreamingResponseBody> exportarNdjson() {
        return resposta(NDJSON, "ndjson",
                saida -> exportacao.percorrer(linha -> escrever(saida, json.writeValueAsString(linha) + "\n")));
    }

    private interface Escrita {
        void em(BufferedWriter saida) throws IOException;
    }

    private ResponseEntity<StreamingResponseBody> resposta(MediaType tipo, String extensao, Escrita escrita) {
        // Roda depois que o método retorna, numa thread do executor do MVC assíncrono, com a resposta já começada:
        // a partir do primeiro byte, o status (200) não muda mais. Um erro no meio corta a resposta.
        StreamingResponseBody corpo = (OutputStream saida) -> {
            var escritor = new BufferedWriter(new OutputStreamWriter(saida, StandardCharsets.UTF_8), 64 * 1024);
            escrita.em(escritor);
            escritor.flush();
        };
        var nome = "catalogo-" + LocalDate.now(relogio) + "." + extensao;
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nome).build()
                        .toString())
                // O StreamingResponseBody não passa pelos ResponseBodyAdvice: a política padrão de cache (20.01) e o
                // Vary da negociação de formato são declarados aqui.
                .cacheControl(CacheControl.noStore())
                .varyBy(HttpHeaders.ACCEPT)
                .body(corpo);
    }

    // O Consumer do percorrer não aceita IOException. Um cliente que desconecta no meio do download faz a escrita
    // falhar: a exceção sobe, a transação de leitura termina e o cursor no banco é fechado.
    private static void escrever(BufferedWriter saida, String linha) {
        try {
            saida.write(linha);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static final class Csv {

        private Csv() {
        }

        /**
         * Um campo de CSV (RFC 4180): entre aspas se tiver vírgula, aspas ou quebra de linha. E protegido contra
         * injeção de fórmula: um valor que começa com =, +, -, @ vira fórmula quando o arquivo é aberto numa planilha
         * ("=HYPERLINK(...)" no nome de um produto). O apóstrofo na frente faz a planilha tratá-lo como texto.
         */
        static String campo(String valor) {
            var seguro = !valor.isEmpty() && "=+-@\t\r".indexOf(valor.charAt(0)) >= 0 ? "'" + valor : valor;
            return seguro.chars().anyMatch(c -> c == ',' || c == '"' || c == '\n' || c == '\r')
                    ? "\"" + seguro.replace("\"", "\"\"") + "\""
                    : seguro;
        }
    }
}
