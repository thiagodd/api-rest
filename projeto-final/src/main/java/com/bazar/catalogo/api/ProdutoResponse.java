package com.bazar.catalogo.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.StatusProduto;
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
        Instant atualizadoEm) {

    static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getSku(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco().emCentavos(),
                produto.getCategoriaId(),
                produto.getStatus(),
                produto.getAtributos(),
                produto.getCriadoEm(),
                produto.getAtualizadoEm());
    }
}
