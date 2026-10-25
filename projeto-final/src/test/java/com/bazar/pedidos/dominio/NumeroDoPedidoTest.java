package com.bazar.pedidos.dominio;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumeroDoPedidoTest {

    @Test
    void formataComOAnoEOSequencialComSeisDigitos() {
        assertThat(NumeroDoPedido.de(123, Instant.parse("2026-10-25T13:30:00Z"))).isEqualTo("BZR-2026-000123");
        assertThat(NumeroDoPedido.de(1_234_567, Instant.parse("2026-10-25T13:30:00Z"))).isEqualTo("BZR-2026-1234567");
    }

    // 1º de janeiro, 01:00 em UTC, ainda é 31 de dezembro em São Paulo: o pedido é do ano anterior (15.01).
    @Test
    void usaOAnoNoFusoDaLoja() {
        assertThat(NumeroDoPedido.de(1, Instant.parse("2027-01-01T01:00:00Z"))).isEqualTo("BZR-2026-000001");
        assertThat(NumeroDoPedido.de(2, Instant.parse("2027-01-01T03:00:00Z"))).isEqualTo("BZR-2027-000002");
    }
}
