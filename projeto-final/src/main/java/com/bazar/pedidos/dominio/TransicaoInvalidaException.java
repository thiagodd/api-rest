package com.bazar.pedidos.dominio;

import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.erros.RegraDeNegocioException;

/**
 * O pedido não pode ir do estado atual para o desejado (ex.: enviar um pedido que não foi pago).
 */
public class TransicaoInvalidaException extends RegraDeNegocioException {

    public TransicaoInvalidaException(UUID pedidoId, StatusPedido atual, StatusPedido desejado) {
        super("transicao-invalida", Map.of("statusAtual", atual, "statusDesejado", desejado),
                pedidoId, atual, desejado);
    }
}
