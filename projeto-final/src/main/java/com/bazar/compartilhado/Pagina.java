package com.bazar.compartilhado;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/**
 * Envelope das coleções paginadas por offset (13.03): os itens, como em Lista, e os metadados da página.
 * O Page do Spring não sai na resposta: o formato do contrato é do Bazar, e não da biblioteca.
 */
public record Pagina<T>(List<T> itens, Metadados pagina) {

    /**
     * @param numero a página atual, a partir de 1, como no parâmetro page
     * @param tamanho quantos itens cabem numa página (o limit aplicado, que pode ser menor que o pedido)
     */
    public record Metadados(int numero, int tamanho, long totalDeItens, int totalDePaginas) {
    }

    public static <E, R> Pagina<R> de(Page<E> pagina, Function<E, R> conversor) {
        return new Pagina<>(pagina.getContent().stream().map(conversor).toList(),
                new Metadados(pagina.getNumber() + 1, pagina.getSize(), pagina.getTotalElements(),
                        pagina.getTotalPages()));
    }
}
