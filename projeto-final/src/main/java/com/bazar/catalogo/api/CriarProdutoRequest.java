package com.bazar.catalogo.api;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Corpo de POST /v1/produtos. Só tem o que o cliente pode escolher:
 * id, status, versão e datas são do Bazar.
 */
public record CriarProdutoRequest(
        @NotBlank @Sku String sku,
        @NotBlank @Size(max = 120) String nome,
        @Nullable @Size(max = 2000) String descricao,
        @NotNull @Positive Long precoEmCentavos,
        @NotNull UUID categoriaId,
        @Size(max = 20) Map<String, String> atributos) {

    public CriarProdutoRequest {
        atributos = atributos == null ? Map.of() : Map.copyOf(atributos); // ausente no JSON → vazio
    }
}
