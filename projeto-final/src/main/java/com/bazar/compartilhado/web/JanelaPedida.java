package com.bazar.compartilhado.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;

/**
 * Os parâmetros da paginação por cursor (13.04): ?limit=20&cursor=... O limit tem as mesmas regras da paginação
 * por offset (PaginaPedida); o cursor é opaco: o cliente só devolve o que recebeu no header Link.
 *
 * @param limit quantos itens por página; acima do máximo, vale o máximo
 * @param cursor a posição de onde continuar; ausente, a listagem começa do início
 */
public record JanelaPedida(
        @Schema(description = "Itens por página. Acima de 100, vale 100.", defaultValue = "20")
        @Nullable @Min(1) Integer limit,
        @Schema(description = "Opaco. Use o valor que veio no header `Link`, sem alterar.")
        @Nullable String cursor) {

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
