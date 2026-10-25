package com.bazar.pedidos.infraestrutura;

import com.bazar.pedidos.dominio.PedidoCriado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reage aos pedidos criados. Por enquanto só registra; em 30.04, o evento passa a sair pelo Outbox.
 */
@Component
class RegistroDePedidosCriados {

    private static final Logger log = LoggerFactory.getLogger(RegistroDePedidosCriados.class);

    // AFTER_COMMIT (o padrão): só roda se a transação que publicou o evento for confirmada.
    // Um pedido desfeito por falta de estoque nunca chega aqui.
    @TransactionalEventListener
    void aoCriarPedido(PedidoCriado evento) {
        log.info("pedido.criado id={} cliente={} total={}", evento.pedidoId(), evento.clienteId(), evento.valorTotal().valor());
    }
}
