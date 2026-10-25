package com.bazar.relatorios.infraestrutura;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Uma linha do relatório de vendas por produto.
 */
public record VendasDoProduto(UUID produtoId, String sku, String nomeProduto, long pedidos, long unidades,
        BigDecimal receita) {
}
