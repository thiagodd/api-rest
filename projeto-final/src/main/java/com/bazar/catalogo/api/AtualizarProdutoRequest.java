package com.bazar.catalogo.api;

import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Corpo de PUT /v1/produtos/{id}. O SKU não aparece: ele não muda depois da criação.
 */
public record AtualizarProdutoRequest(
        String nome,
        @Nullable String descricao,
        Long precoEmCentavos,
        UUID categoriaId,
        Map<String, String> atributos) {

    public AtualizarProdutoRequest {
        atributos = atributos == null ? Map.of() : Map.copyOf(atributos); // ausente no JSON → vazio
    }
}
