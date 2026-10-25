package com.bazar.compartilhado.web;

import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Os parâmetros de paginação por offset (13.03): ?page=1&limit=20. Um record com @ModelAttribute, e não o
 * Pageable do Spring, porque o Pageable transforma valores inválidos (page=-1, limit=abc) na primeira página com
 * 20 itens, em silêncio. Aqui, um valor que não converte é 400 e um valor menor que 1 é 422.
 *
 * @param page a página pedida, a partir de 1
 * @param limit quantos itens por página; acima do máximo, vale o máximo (a resposta mostra o tamanho aplicado)
 */
public record PaginaPedida(@Nullable @Min(1) Integer page, @Nullable @Min(1) Integer limit) {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    public int numero() {
        return page == null ? 1 : page;
    }

    public int tamanho() {
        return limit == null ? TAMANHO_PADRAO : Math.min(limit, TAMANHO_MAXIMO);
    }

    // O Spring Data conta as páginas a partir de 0.
    public Pageable ordenadaPor(Sort ordenacao) {
        return PageRequest.of(numero() - 1, tamanho(), ordenacao);
    }
}
