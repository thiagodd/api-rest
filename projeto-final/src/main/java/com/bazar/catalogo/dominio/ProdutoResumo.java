package com.bazar.catalogo.dominio;

import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;

/**
 * Projeção de leitura para listagens (10.05): só as colunas que a lista mostra, sem descrição nem atributos.
 * Os nomes e tipos dos componentes precisam bater com as propriedades de Produto.
 */
public record ProdutoResumo(UUID id, String sku, String nome, Dinheiro preco, StatusProduto status, UUID categoriaId) {
}
