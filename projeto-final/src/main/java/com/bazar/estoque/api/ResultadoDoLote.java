package com.bazar.estoque.api;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import org.springframework.http.ProblemDetail;

/**
 * O resultado de um lote parcial (14.04): um item por item do pedido, na mesma ordem, com o status e o estoque
 * (sucesso) ou o Problem Details (falha). Os totais poupam o cliente de contar.
 */
public record ResultadoDoLote(int sucessos, int falhas, List<Item> itens) {

    static ResultadoDoLote de(List<Item> itens) {
        var sucessos = (int) itens.stream().filter(item -> item.problema() == null).count();
        return new ResultadoDoLote(sucessos, itens.size() - sucessos, itens);
    }

    // Sem o nome, os quatro records Item do Bazar viram um esquema só no contrato (17.02).
    @Schema(name = "ResultadoDoItem")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Item(
            int indice,
            UUID produtoId,
            int status,
            @Nullable EstoqueResponse estoque,
            @Nullable ProblemDetail problema) {
    }
}
