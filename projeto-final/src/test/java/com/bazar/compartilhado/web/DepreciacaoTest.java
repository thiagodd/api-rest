package com.bazar.compartilhado.web;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DepreciacaoTest {

    private static final Depreciacao PRODUTO_V1 = Depreciacao.CALENDARIO.getFirst();

    @Test
    void valeParaOsCaminhosDoRecursoNaVersaoDepreciada() {
        assertThat(PRODUTO_V1.vale("/v1/produtos")).isTrue();
        assertThat(PRODUTO_V1.vale("/v1/produtos/buscas")).isTrue();
        assertThat(PRODUTO_V1.vale("/v1/produtos/01a17566-c2c2-750b-a6fd-e44a3e704746")).isTrue();
    }

    @Test
    void naoValeParaOutraVersaoNemParaOEstoque() {
        // O estoque fica sob /v1/produtos/{id}, mas é outro recurso, sem v2 (ADR-0003).
        assertThat(PRODUTO_V1.vale("/v2/produtos/01a17566-c2c2-750b-a6fd-e44a3e704746")).isFalse();
        assertThat(PRODUTO_V1.vale("/v1/produtos/01a17566-c2c2-750b-a6fd-e44a3e704746/estoque")).isFalse();
    }

    @Test
    void soEstaDesligadaDepoisDoSunset() {
        var caminho = "/v1/produtos/01a17566-c2c2-750b-a6fd-e44a3e704746";
        assertThat(Depreciacao.desligada(caminho, Instant.parse("2027-04-29T23:59:59Z"))).isEmpty();
        assertThat(Depreciacao.desligada(caminho, Instant.parse("2027-04-30T00:00:00Z"))).contains(PRODUTO_V1);
    }
}
