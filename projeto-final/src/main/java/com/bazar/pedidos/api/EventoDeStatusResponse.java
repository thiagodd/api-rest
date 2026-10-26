package com.bazar.pedidos.api;

import java.time.Instant;
import java.util.UUID;

import com.bazar.pedidos.dominio.MudancaDeStatus;
import com.bazar.pedidos.dominio.StatusPedido;

/**
 * O campo data de um evento "status" do acompanhamento do pedido (19.04).
 */
public record EventoDeStatusResponse(UUID pedidoId, String numero, StatusPedido status, Instant ocorridoEm) {

    static EventoDeStatusResponse de(MudancaDeStatus mudanca) {
        return new EventoDeStatusResponse(mudanca.pedidoId(), mudanca.numero(), mudanca.status(),
                mudanca.ocorridoEm());
    }
}
