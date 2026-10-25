package com.bazar.pedidos.dominio;

import java.time.Instant;
import java.time.ZoneId;

/**
 * O número legível de um pedido (15.03): BZR, o ano da compra no fuso da loja e um sequencial com 6 dígitos.
 * O sequencial vem de uma sequence do banco e não recomeça a cada ano: o ano é informativo, e a unicidade vem do
 * sequencial.
 */
public final class NumeroDoPedido {

    private static final ZoneId FUSO_DA_LOJA = ZoneId.of("America/Sao_Paulo");

    private NumeroDoPedido() {
    }

    public static String de(long sequencial, Instant criadoEm) {
        return "BZR-%d-%06d".formatted(criadoEm.atZone(FUSO_DA_LOJA).getYear(), sequencial);
    }
}
