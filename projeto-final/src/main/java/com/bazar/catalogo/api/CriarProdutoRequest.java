package com.bazar.catalogo.api;

import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.Produto;
import com.bazar.compartilhado.Dinheiro;
import org.jspecify.annotations.Nullable;

/**
 * Corpo de POST /v1/produtos. Só tem o que o cliente pode escolher:
 * id, status, versão e datas são do Bazar.
 */
public record CriarProdutoRequest(
        String sku,
        String nome,
        @Nullable String descricao,
        Long precoEmCentavos,
        UUID categoriaId,
        Map<String, String> atributos) {

    public CriarProdutoRequest {
        atributos = atributos == null ? Map.of() : Map.copyOf(atributos); // ausente no JSON → vazio
    }

    Produto paraProduto() {
        return new Produto(sku, nome, descricao, Dinheiro.deCentavos(precoEmCentavos, "BRL"), categoriaId, atributos);
    }
}
