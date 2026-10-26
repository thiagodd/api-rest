package com.bazar.relatorios.api;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import com.bazar.compartilhado.operacoes.OperacaoController;
import com.bazar.compartilhado.operacoes.StatusOperacao;
import com.bazar.compartilhado.web.DownloadDeArquivo;
import com.bazar.relatorios.dominio.PeriodoDeVendas;
import com.bazar.relatorios.dominio.RelatorioVendas;
import com.bazar.relatorios.dominio.RelatorioVendasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ETag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;
import static com.bazar.compartilhado.web.RespostasDeErro.VALIDACAO;

@RestController
@RequestMapping("/v1/relatorios")
@Tag(name = "relatorios", description = "Relatórios gerados fora da requisição")
public class RelatorioController {

    private static final Logger log = LoggerFactory.getLogger(RelatorioController.class);

    private final RelatorioVendasService relatorios;

    public RelatorioController(RelatorioVendasService relatorios) {
        this.relatorios = relatorios;
    }

    // 202 Accepted: o pedido foi registrado, e o relatório será gerado depois (19.03). O Location aponta o recurso da
    // operação, que o cliente consulta no ritmo do Retry-After até receber o 303 para o relatório (21.02).
    @PostMapping("/vendas")
    @Operation(operationId = "solicitarRelatorioDeVendas", summary = "Solicita o relatório de vendas por produto",
            description = "O relatório é gerado fora da requisição. Acompanhe a operação no `Location`.")
    @ApiResponse(responseCode = "202", description = "O pedido foi registrado e o relatório será gerado", headers = {
            @Header(name = "Location", description = "A operação, para acompanhar", schema = @Schema(type = "string")),
            @Header(name = "Retry-After", description = "Segundos até a primeira consulta",
                    schema = @Schema(type = "integer")) })
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ResponseEntity<OperacaoAceitaResponse> solicitarVendas(
            @Valid @RequestBody SolicitarRelatorioVendasRequest pedido) {
        var operacao = relatorios.solicitar(pedido.paraPeriodo());
        // O MDC vai junto para a thread do @Async (spring.task.execution.propagate-context).
        MDC.put("operacao", operacao.toString());
        try {
            relatorios.gerarAsync(operacao);
        }
        catch (TaskRejectedException ex) {
            // O executor está no limite. A operação já está gravada: o ProcessadorDeOperacoes a pega depois.
            log.info("Executor cheio; a operação {} fica para o processador", operacao);
        }
        finally {
            MDC.remove("operacao");
        }
        return ResponseEntity.accepted()
                .location(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(OperacaoController.caminho(operacao)).build().toUri())
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(OperacaoController.INTERVALO_DE_CONSULTA.toSeconds()))
                .body(new OperacaoAceitaResponse(operacao, StatusOperacao.PENDENTE));
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "Os dados de um relatório pronto")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public RelatorioResponse detalhar(@PathVariable UUID id) {
        return RelatorioResponse.de(relatorios.buscar(id));
    }

    // Um arquivo pronto, servido como Resource: o Spring copia em blocos, põe o Content-Length e atende o Range (206).
    // Um download interrompido continua de onde parou, sem baixar tudo de novo. O If-Range é do DownloadDeArquivo.
    @GetMapping("/{id}/arquivo")
    @Operation(operationId = "baixarRelatorio", summary = "Baixa o arquivo do relatório",
            description = "CSV em UTF-8. Aceita `Range: bytes=...` (`206 Partial Content`), para continuar um download "
                    + "interrompido; com `If-Range`, só se o arquivo não mudou.")
    @ApiResponse(responseCode = "200", description = "O arquivo inteiro",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "206", description = "O trecho pedido no Range",
            content = @Content(mediaType = "text/csv", schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    @ApiResponse(responseCode = "416", description = "O Range pedido está fora do arquivo", content = @Content)
    public ResponseEntity<Resource> baixar(@PathVariable UUID id, @RequestHeader HttpHeaders requisicao) {
        var relatorio = relatorios.buscar(id);
        var periodo = relatorio.periodo();
        var nome = "vendas-%s-a-%s.csv".formatted(LocalDate.ofInstant(periodo.de(), ZoneOffset.UTC),
                LocalDate.ofInstant(periodo.ate(), ZoneOffset.UTC));
        // O relatório não muda depois de gerado: o id identifica o conteúdo, e serve de ETag para o If-Range.
        return DownloadDeArquivo.de(requisicao, new FileSystemResource(relatorio.arquivo()),
                ETag.create("\"" + id + "\""), relatorio.geradoEm(), ResponseEntity.ok()
                        .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nome).build()
                                .toString())
                        .cacheControl(CacheControl.noCache().cachePrivate()));
    }

    public record OperacaoAceitaResponse(UUID operacaoId, StatusOperacao status) {
    }

    // O caminho do arquivo no servidor não sai daqui: o download é pelo link arquivo (22.02).
    public record RelatorioResponse(UUID id, PeriodoDeVendas periodo, String formato, long tamanhoEmBytes,
            Instant geradoEm, @Schema(description = "Onde baixar o arquivo") String arquivo) {

        static RelatorioResponse de(RelatorioVendas relatorio) {
            return new RelatorioResponse(relatorio.id(), relatorio.periodo(), "text/csv", relatorio.tamanhoEmBytes(),
                    relatorio.geradoEm(), ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/v1/relatorios/{id}/arquivo").buildAndExpand(relatorio.id()).toUriString());
        }
    }
}
