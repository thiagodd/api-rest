package com.bazar.pedidos.api;

import com.bazar.clientes.api.ClienteController;
import com.bazar.pedidos.dominio.Pedido;
import com.bazar.pedidos.dominio.StatusPedido;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A representação do pedido com os links do estado atual (03.04): o cliente descobre o que pode fazer
 * pelos links presentes, e não repetindo as regras da máquina de estados.
 */
@Component
public class PedidoModelAssembler implements RepresentationModelAssembler<Pedido, EntityModel<PedidoResponse>> {

    @Override
    public EntityModel<PedidoResponse> toModel(Pedido pedido) {
        var modelo = EntityModel.of(PedidoResponse.de(pedido),
                linkTo(methodOn(PedidoController.class).detalhar(pedido.getId())).withSelfRel(),
                // methodOn não serve aqui: ele cria uma subclasse do tipo de retorno do método, e o
                // ClienteResponse é um record (final). Com linkTo(classe), o caminho vem do @RequestMapping.
                linkTo(ClienteController.class).slash(pedido.getClienteId()).withRel("cliente"));
        // O link só aparece quando a ação é possível. A decisão é do domínio, não deste assembler.
        if (pedido.getStatus().podeIrPara(StatusPedido.CANCELADO)) {
            modelo.add(linkTo(methodOn(PedidoAcoesController.class).cancelar(pedido.getId(), null))
                    .withRel(Relacoes.CANCELAR));
        }
        return modelo;
    }
}
