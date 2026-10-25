package com.bazar.pedidos.api;

import java.util.UUID;

import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.pedidos.dominio.PedidoService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
public class PedidoController {

    private final PedidoService servico;

    public PedidoController(PedidoService servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody CriarPedidoRequest request) {
        var pedido = servico.criar(request.clienteId(), request.itensSolicitados());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pedido.getId())
                .toUri();
        return ResponseEntity.created(location).body(PedidoResponse.de(pedido));
    }

    @GetMapping
    public Lista<PedidoResponse> listar(@RequestParam UUID clienteId) {
        return new Lista<>(servico.listarDoCliente(clienteId, 20).stream().map(PedidoResponse::de).toList());
    }

    @GetMapping("/{id}")
    public PedidoResponse detalhar(@PathVariable UUID id) {
        return servico.buscar(id)
                .map(PedidoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("pedido-nao-encontrado", id));
    }
}
