package com.bazar.pedidos.api;

import java.util.UUID;

import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.compartilhado.idempotencia.Idempotente;
import com.bazar.compartilhado.idempotencia.IdempotenciaFilter;
import com.bazar.compartilhado.web.JanelaPedida;
import com.bazar.compartilhado.web.LinksDeCursor;
import com.bazar.compartilhado.web.RespostasDeErro;
import com.bazar.pedidos.dominio.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// O controller não tem @Transactional: a transação começa e termina no serviço.
@RestController
@RequestMapping("/v1/pedidos")
@Tag(name = "pedidos", description = "Os pedidos")
public class PedidoController {

    private final PedidoService servico;
    private final PedidoModelAssembler assembler;

    public PedidoController(PedidoService servico, PedidoModelAssembler assembler) {
        this.servico = servico;
        this.assembler = assembler;
    }

    @PostMapping
    @Idempotente
    @Operation(operationId = "criarPedido", summary = "Cria um pedido e reserva o estoque",
            description = "Um cliente ou produto inexistente dá `422`; estoque insuficiente, `409` (10.03).")
    @Parameter(in = ParameterIn.HEADER, name = IdempotenciaFilter.IDEMPOTENCY_KEY, schema = @Schema(type = "string"),
            description = """
                    Uma chave única por pedido (um UUID novo), entre aspas: `"8e03978e-40d5-43e8-bc93-6894a57f9324"`. \
                    Repetir com a mesma chave e o mesmo corpo devolve a resposta original, com \
                    `Idempotent-Replayed: true`, sem criar outro pedido. A chave vale por 24 horas. A mesma chave \
                    com outro corpo dá `422`; enquanto a primeira ainda está em andamento, `409` (21.01).""")
    @ApiResponse(responseCode = "201", description = "O pedido foi criado", headers = @Header(name = "Location",
            description = "A URI do pedido", schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "409", ref = RespostasDeErro.CONFLITO)
    @ApiResponse(responseCode = "422", ref = RespostasDeErro.VALIDACAO)
    public ResponseEntity<EntityModel<PedidoResponse>> criar(@Valid @RequestBody CriarPedidoRequest request) {
        var pedido = servico.criar(request.clienteId(), request.itensSolicitados());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pedido.getId())
                .toUri();
        return ResponseEntity.created(location).body(assembler.toModel(pedido));
    }

    // A lista não traz _links: as ações aparecem no detalhe de cada pedido (03.04, "os custos").
    // Paginada por cursor (13.04): ?limit=20&cursor=..., com first, prev e next no header Link.
    @GetMapping
    public ResponseEntity<Lista<PedidoResponse>> listar(@RequestParam UUID clienteId,
            @Valid @ModelAttribute JanelaPedida janela) {
        var posicao = CursorDePedidos.posicao(janela.cursor(), clienteId);
        var pedidos = servico.listarDoCliente(clienteId, posicao, janela.tamanho());
        return ResponseEntity.ok()
                .headers(LinksDeCursor.de(pedidos, posicao,
                        (chaves, paraTras) -> CursorDePedidos.de(clienteId, chaves, paraTras)))
                .body(new Lista<>(pedidos.stream().map(PedidoResponse::de).toList()));
    }

    @GetMapping("/{id}")
    public EntityModel<PedidoResponse> detalhar(@PathVariable UUID id) {
        return servico.buscar(id)
                .map(assembler::toModel)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.PEDIDO_NAO_ENCONTRADO, id));
    }
}
