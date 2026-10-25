package com.bazar.catalogo.dominio;

import java.util.Map;

import com.bazar.compartilhado.erros.RegraDeNegocioException;

/**
 * Já existe um produto com o SKU informado (409). Encontrada pelo teste de integração da 11.04: antes dela,
 * a constraint unique do banco recusava o INSERT e o cliente recebia um 500.
 */
public class SkuDuplicadoException extends RegraDeNegocioException {

    public SkuDuplicadoException(String sku) {
        super("sku-duplicado", Map.of("sku", sku), sku);
    }
}
