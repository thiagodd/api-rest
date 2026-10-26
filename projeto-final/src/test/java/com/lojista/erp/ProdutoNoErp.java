package com.lojista.erp;

/** O produto como o ERP do lojista o guarda: só os três campos que ele usa da API do Bazar. */
public record ProdutoNoErp(String sku, String nome, long precoEmCentavos) {
}
