package com.bazar.pedidos.api;

import java.util.UUID;

import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.pedidos.dominio.HistoricoDoPedido;
import com.bazar.pedidos.dominio.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.Nullable;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;

@RestController
@Tag(name = "pedidos")
public class EventosDoPedidoController {

    private final PedidoService pedidos;
    private final HistoricoDoPedido historico;
    private final AcompanhamentoDePedidos acompanhamento;

    EventosDoPedidoController(PedidoService pedidos, HistoricoDoPedido historico,
            AcompanhamentoDePedidos acompanhamento) {
        this.pedidos = pedidos;
        this.historico = historico;
        this.acompanhamento = acompanhamento;
    }

    // As mudanças de status do pedido em tempo real, como Server-Sent Events (19.04).
    @GetMapping(path = "/v1/pedidos/{id}/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(operationId = "acompanharPedido", summary = "Acompanha as mudanças de status do pedido (SSE)",
            description = "Cada mudança é um evento `status`, com o `id` crescente. Ao reconectar, o cliente manda o "
                    + "último id recebido em `Last-Event-ID` e recebe só os seguintes. Um pedido em estado final "
                    + "encerra o fluxo; a reconexão depois disso recebe `204`.")
    // Sem isto, o springdoc descreve o SseEmitter (uma classe do Spring) como se fosse o corpo. O OpenAPI 3.1 não
    // tem como descrever cada evento do fluxo; o itemSchema chegou no 3.2.
    @ApiResponse(responseCode = "200", description = "O fluxo de eventos. O `data` de cada evento `status` é um "
            + "objeto com `pedidoId`, `numero`, `status` e `ocorridoEm`.",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "204", description = "O pedido está num estado final, e não há eventos novos",
            content = @Content)
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public ResponseEntity<SseEmitter> acompanhar(@PathVariable UUID id,
            @Parameter(description = "O id do último evento recebido, enviado pelo EventSource ao reconectar.")
            @RequestHeader(name = "Last-Event-ID", required = false) @Nullable Long ultimoVisto) {
        var pedido = pedidos.buscar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.PEDIDO_NAO_ENCONTRADO, id));
        var novidades = historico.depoisDe(id, ultimoVisto == null ? 0 : ultimoVisto);
        if (pedido.getStatus().ehFinal() && novidades.isEmpty() && ultimoVisto != null) {
            // Pela especificação do HTML, um 204 faz o EventSource parar de reconectar.
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                // Nenhum cache guarda um fluxo, e o nginx não deve acumular os eventos antes de repassá-los.
                .cacheControl(CacheControl.noStore())
                .header("X-Accel-Buffering", "no")
                .body(acompanhamento.acompanhar(id, novidades, pedido.getStatus()));
    }
}
