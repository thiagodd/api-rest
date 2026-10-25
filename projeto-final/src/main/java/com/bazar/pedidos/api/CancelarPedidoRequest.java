package com.bazar.pedidos.api;

import com.bazar.pedidos.dominio.MotivoCancelamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record CancelarPedidoRequest(
        @NotNull MotivoCancelamento motivo,
        @Nullable @Size(max = 500) String observacao) {
}
