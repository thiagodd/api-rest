package com.bazar.pedidos.dominio;

/**
 * Os estados do pedido. As transições (pagar, cancelar, enviar...) chegam em 12.04.
 */
public enum StatusPedido {
    CRIADO,
    PAGAMENTO_PENDENTE,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO,
    REEMBOLSADO
}
