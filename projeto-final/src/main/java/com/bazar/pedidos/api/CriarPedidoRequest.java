package com.bazar.pedidos.api;

import java.util.List;
import java.util.UUID;

import com.bazar.pedidos.dominio.ItemSolicitado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Corpo de POST /v1/pedidos. O cliente diz o que quer e quanto; preços e totais são calculados pelo Bazar.
 */
public record CriarPedidoRequest(
        @NotNull UUID clienteId,
        @NotEmpty @Size(max = 50) List<@Valid @NotNull Item> itens) {

    public record Item(@NotNull UUID produtoId, @NotNull @Positive @Max(100) Integer quantidade) {
    }

    List<ItemSolicitado> itensSolicitados() {
        return itens.stream().map(item -> new ItemSolicitado(item.produtoId(), item.quantidade())).toList();
    }
}
