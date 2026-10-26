package com.bazar.estoque.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.jspecify.annotations.Nullable;

/**
 * Corpo de PUT /v1/produtos/{id}/estoque: o novo saldo disponível. A versão lida vai no If-Match (20.03); o campo
 * versao, o jeito da 10.06, continua aceito para os clientes que já o mandam.
 */
public record DefinirEstoqueRequest(
        @NotNull @PositiveOrZero @Max(1_000_000) Integer quantidadeDisponivel,
        @Schema(deprecated = true, description = "Use o header If-Match com o ETag do GET.") @Nullable Long versao) {
}
