package com.bazar.pedidos.api;

import java.util.UUID;

import com.bazar.pedidos.dominio.PedidoService;
import jakarta.validation.Valid;

import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * As ações sobre um pedido que não são CRUD (12.04). Cada ação é um POST num sub-recurso com nome de substantivo,
 * e a regra (pode ou não pode) fica no domínio: Pedido e StatusPedido.
 */
@RestController
@RequestMapping("/v1/pedidos/{id}")
public class PedidoAcoesController {

    private final PedidoService servico;
    private final PedidoModelAssembler assembler;

    public PedidoAcoesController(PedidoService servico, PedidoModelAssembler assembler) {
        this.servico = servico;
        this.assembler = assembler;
    }

    @PostMapping("/cancelamento")
    public EntityModel<PedidoResponse> cancelar(@PathVariable UUID id, @Valid @RequestBody CancelarPedidoRequest request) {
        return assembler.toModel(servico.cancelar(id, request.motivo(), request.observacao()));
    }
}
