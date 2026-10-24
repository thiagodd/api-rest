package com.bazar.catalogo.api;

import jakarta.validation.constraints.PositiveOrZero;
import org.jspecify.annotations.Nullable;

/**
 * Faixa de preço em centavos dos filtros da listagem de produtos (13.01). Os dois limites são opcionais.
 */
@IntervaloPrecoValido
public record IntervaloPreco(
        @Nullable @PositiveOrZero Long precoMin,
        @Nullable @PositiveOrZero Long precoMax) {
}
