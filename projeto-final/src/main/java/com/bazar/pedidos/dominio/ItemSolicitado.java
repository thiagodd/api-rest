package com.bazar.pedidos.dominio;

import java.util.UUID;

/**
 * Um item como o cliente pediu: qual produto e quantas unidades. O preço vem do catálogo, nunca do cliente.
 */
public record ItemSolicitado(UUID produtoId, int quantidade) {
}
