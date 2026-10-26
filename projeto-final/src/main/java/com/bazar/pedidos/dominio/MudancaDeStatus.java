package com.bazar.pedidos.dominio;

import java.time.Instant;
import java.util.UUID;

/**
 * Uma linha do histórico do pedido (19.04). O id cresce a cada mudança, em todos os pedidos: é o id do evento
 * enviado por SSE e o que o cliente devolve no Last-Event-ID.
 */
public record MudancaDeStatus(long id, UUID pedidoId, String numero, StatusPedido status, Instant ocorridoEm) {
}
