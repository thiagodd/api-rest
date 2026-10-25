package com.bazar.catalogo.dominio;

import java.math.BigDecimal;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Os filtros da listagem de produtos. Um campo nulo não filtra. Os parâmetros HTTP chegam em 13.01.
 */
public record FiltroDeProdutos(
        @Nullable UUID categoriaId,
        @Nullable StatusProduto status,
        @Nullable BigDecimal precoMinimo,
        @Nullable BigDecimal precoMaximo,
        @Nullable String texto) {

    public static FiltroDeProdutos nenhum() {
        return new FiltroDeProdutos(null, null, null, null, null);
    }
}
