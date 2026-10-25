package com.bazar.pedidos.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;
import com.bazar.pedidos.dominio.Pedido;
import com.bazar.pedidos.dominio.StatusPedido;

/**
 * Representação de um pedido. Os valores usam o tipo Dinheiro ({"valor": "129.90", "moeda": "BRL"}).
 */
public record PedidoResponse(
        UUID id,
        UUID clienteId,
        StatusPedido status,
        List<Item> itens,
        Dinheiro valorItens,
        Dinheiro valorFrete,
        Dinheiro valorTotal,
        Instant criadoEm) {

    public record Item(UUID produtoId, String sku, String nomeProduto, int quantidade, Dinheiro precoUnitario,
            Dinheiro subtotal) {
    }

    static PedidoResponse de(Pedido pedido) {
        var itens = pedido.getItens().stream()
                .map(item -> new Item(item.getProdutoId(), item.getSku(), item.getNomeProduto(), item.getQuantidade(),
                        new Dinheiro(item.getPrecoUnitario(), pedido.getMoeda()),
                        new Dinheiro(item.getSubtotal(), pedido.getMoeda())))
                .toList();
        return new PedidoResponse(pedido.getId(), pedido.getClienteId(), pedido.getStatus(), itens,
                pedido.getValorItens(), pedido.getValorFrete(), pedido.getValorTotal(), pedido.getCriadoEm());
    }
}
