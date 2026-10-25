package com.bazar.catalogo.api;

import java.math.BigDecimal;

import org.jspecify.annotations.Nullable;

/**
 * Uma faixa de preço opcional, em centavos, como o precoEmCentavos das respostas da v1. Implementada pelos
 * filtros da listagem e da busca de produtos, que compartilham a validação @IntervaloPrecoValido (13.01).
 */
public interface IntervaloDePreco {

    @Nullable Long precoMin();

    @Nullable Long precoMax();

    // O domínio trabalha em reais (Dinheiro.valor); a conversão fica na borda, junto do contrato.
    static @Nullable BigDecimal emReais(@Nullable Long centavos) {
        return centavos == null ? null : BigDecimal.valueOf(centavos, 2);
    }
}
