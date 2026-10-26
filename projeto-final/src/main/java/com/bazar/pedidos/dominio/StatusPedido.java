package com.bazar.pedidos.dominio;

import java.util.EnumSet;
import java.util.Set;

/**
 * Os estados do pedido e as transições permitidas entre eles. As ações da API que disparam as transições
 * (pagar, cancelar, enviar...) chegam em 12.04.
 */
public enum StatusPedido {
    CRIADO,
    PAGAMENTO_PENDENTE,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO,
    REEMBOLSADO;

    // ENTREGUE, CANCELADO e REEMBOLSADO são finais: não saem para nenhum outro estado.
    public boolean podeIrPara(StatusPedido destino) {
        return destinosPermitidos().contains(destino);
    }

    // Um pedido final não muda mais: o acompanhamento em tempo real (19.04) pode encerrar.
    public boolean ehFinal() {
        return destinosPermitidos().isEmpty();
    }

    private Set<StatusPedido> destinosPermitidos() {
        return switch (this) {
            case CRIADO -> EnumSet.of(PAGAMENTO_PENDENTE, CANCELADO);
            case PAGAMENTO_PENDENTE -> EnumSet.of(PAGO, CANCELADO);
            case PAGO -> EnumSet.of(ENVIADO, REEMBOLSADO);
            case ENVIADO -> EnumSet.of(ENTREGUE);
            case ENTREGUE, CANCELADO, REEMBOLSADO -> EnumSet.noneOf(StatusPedido.class);
        };
    }
}
