package com.bazar.pedidos.dominio;

import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;

/**
 * Evento publicado quando um pedido é criado. Os interessados só o recebem depois do commit (10.03);
 * a entrega confiável para outros sistemas, com o padrão Outbox, fica para 30.04.
 */
public record PedidoCriado(UUID pedidoId, UUID clienteId, Dinheiro valorTotal) {
}
