package com.bazar.compartilhado.web;

import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;

/**
 * Os parâmetros da paginação por cursor (13.04): ?limit=20&cursor=... O limit tem as mesmas regras da paginação
 * por offset (PaginaPedida); o cursor é opaco: o cliente só devolve o que recebeu no header Link.
 *
 * @param limit quantos itens por página; acima do máximo, vale o máximo
 * @param cursor a posição de onde continuar; ausente, a listagem começa do início
 */
public record JanelaPedida(@Nullable @Min(1) Integer limit, @Nullable String cursor) {

    // ?cursor= vazio é o mesmo que não mandar o cursor, como o q da busca (13.01).
    public JanelaPedida {
        if (cursor != null && cursor.isBlank()) {
            cursor = null;
        }
    }

    public int tamanho() {
        return limit == null ? PaginaPedida.TAMANHO_PADRAO : Math.min(limit, PaginaPedida.TAMANHO_MAXIMO);
    }
}
