package com.bazar.relatorios.api;

import java.util.UUID;

import com.bazar.compartilhado.operacoes.StatusOperacao;
import com.bazar.relatorios.dominio.RelatorioVendasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 202 Accepted: o pedido foi registrado, e o relatório será gerado depois (19.03). O recurso para acompanhar a
    // operação (GET /v1/operacoes/{id}, com Location e Retry-After) chega na 21.02.
    @PostMapping("/vendas")
    @Operation(operationId = "solicitarRelatorioDeVendas", summary = "Solicita o relatório de vendas por produto",
            description = "O relatório é gerado fora da requisição. A resposta só confirma o registro do pedido.")
    @ApiResponse(responseCode = "202", description = "O pedido foi registrado e o relatório será gerado")
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
        return ResponseEntity.accepted().body(new OperacaoAceitaResponse(operacao, StatusOperacao.PENDENTE));
    }

    public record OperacaoAceitaResponse(UUID operacaoId, StatusOperacao status) {
    }
}
