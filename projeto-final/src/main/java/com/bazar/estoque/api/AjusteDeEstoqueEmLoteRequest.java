package com.bazar.estoque.api;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Um lote de ajustes de estoque (14.04). Cada item define a quantidade disponível de um produto: um valor
 * absoluto, e não um "some 5", para que reenviar o lote dê o mesmo resultado.
 */
public record AjusteDeEstoqueEmLoteRequest(
        @NotEmpty @Size(max = AjusteDeEstoqueEmLoteRequest.MAXIMO) List<@Valid @NotNull Item> itens) {

    public static final int MAXIMO = 500;

    public record Item(
            @NotNull UUID produtoId,
            @NotNull @PositiveOrZero @Max(1_000_000) Integer quantidadeDisponivel) {
    }
}
