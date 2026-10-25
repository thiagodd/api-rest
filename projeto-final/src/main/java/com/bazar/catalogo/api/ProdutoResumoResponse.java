package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.StatusProduto;

/**
 * Um produto na listagem (10.05). O detalhe completo, com descrição e atributos, fica em GET /v1/produtos/{id}.
 */
public record ProdutoResumoResponse(
        UUID id,
        String sku,
        String nome,
        long precoEmCentavos,
        UUID categoriaId,
        StatusProduto status) {
}
