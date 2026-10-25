package com.bazar.estoque.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Corpo de PUT /v1/produtos/{id}/estoque: o novo saldo disponível e a versão que o cliente leu.
 */
public record DefinirEstoqueRequest(
        @NotNull @PositiveOrZero @Max(1_000_000) Integer quantidadeDisponivel,
        @NotNull Long versao) {
}
