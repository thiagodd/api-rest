package com.bazar.relatorios.api;

import java.time.Instant;
import java.util.UUID;

import com.bazar.compartilhado.operacoes.OperacaoController;
import com.bazar.compartilhado.operacoes.StatusOperacao;
import com.bazar.relatorios.dominio.PeriodoDeVendas;
import com.bazar.relatorios.dominio.RelatorioVendas;
import com.bazar.relatorios.dominio.RelatorioVendasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    public record OperacaoAceitaResponse(UUID operacaoId, StatusOperacao status) {
    }

    // O caminho do arquivo no servidor não sai daqui: o download chega na 22.02, por um link.
    public record RelatorioResponse(UUID id, PeriodoDeVendas periodo, String formato, long tamanhoEmBytes,
            Instant geradoEm) {

        static RelatorioResponse de(RelatorioVendas relatorio) {
            return new RelatorioResponse(relatorio.id(), relatorio.periodo(), "text/csv", relatorio.tamanhoEmBytes(),
                    relatorio.geradoEm());
        }
    }
}
