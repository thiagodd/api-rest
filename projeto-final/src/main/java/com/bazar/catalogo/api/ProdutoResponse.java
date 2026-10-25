package com.bazar.catalogo.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.StatusProduto;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * Representação v1 do produto. A versão interna (usada para concorrência) não é exposta.
 */
public record ProdutoResponse(
        UUID id,
        String sku,
        String nome,
        @Nullable String descricao,
        long precoEmCentavos,
        UUID categoriaId,
        StatusProduto status,
        Map<String, String> atributos,
        Instant criadoEm,
        Instant atualizadoEm,
        // Só com ?embed=categoria (13.05); sem ele, o campo não aparece.
        @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable CategoriaResponse categoria) {

    ProdutoResponse comCategoria(CategoriaResponse categoria) {
        return new ProdutoResponse(id, sku, nome, descricao, precoEmCentavos, categoriaId, status, atributos, criadoEm,
                atualizadoEm, categoria);
    }
}
