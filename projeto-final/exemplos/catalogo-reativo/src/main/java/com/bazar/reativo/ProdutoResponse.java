package com.bazar.reativo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Um recorte da representação v1 do produto do Bazar: o suficiente para comparar.
 */
public record ProdutoResponse(UUID id, String sku, String nome, @Nullable String descricao, Preco preco,
        UUID categoriaId, String status, Instant atualizadoEm) {

    public record Preco(BigDecimal valor, String moeda) {
    }
}
