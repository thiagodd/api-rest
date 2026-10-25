package com.bazar.catalogo.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.StatusProduto;
import com.bazar.compartilhado.Dinheiro;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/**
 * Representação v1 do produto. A versão interna (usada para concorrência) não é exposta.
 */
public record ProdutoResponse(
        UUID id,
        String sku,
        String nome,
        @Nullable String descricao,
        // Expand-contract (18.01): o preco entrou ao lado do precoEmCentavos, que continua na v1 e sai na v2 (18.03).
        @Schema(deprecated = true, description = "Use `preco`. Sai na v2.") long precoEmCentavos,
        Dinheiro preco,
        UUID categoriaId,
        StatusProduto status,
        Map<String, String> atributos,
        Instant criadoEm,
        Instant atualizadoEm,
        // Só com ?embed=categoria (13.05); sem ele, o campo não aparece.
        @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable CategoriaResponse categoria) {

    ProdutoResponse comCategoria(CategoriaResponse categoria) {
        return new ProdutoResponse(id, sku, nome, descricao, precoEmCentavos, preco, categoriaId, status, atributos,
                criadoEm, atualizadoEm, categoria);
    }
}
