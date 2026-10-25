package com.bazar.compartilhado.web;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * O header Link (RFC 8288) de uma coleção paginada por cursor (13.04): first (sem cursor), prev e next. Cada URI
 * é a requisição atual com outro cursor; os demais parâmetros (filtros, sort, limit) são preservados. Não há
 * last: o cursor não sabe quantas páginas existem.
 */
public final class LinksDeCursor {

    private LinksDeCursor() {
    }

    /**
     * @param janela os itens da página, na ordem da listagem, com as posições de cada um
     * @param pedida a posição de onde a página partiu
     * @param cursor o cursor de uma posição (as chaves de um item), para a frente (false) ou para trás (true)
     */
    public static HttpHeaders de(Window<?> janela, KeysetScrollPosition pedida,
            BiFunction<Map<String, ?>, Boolean, String> cursor) {
        var links = new ArrayList<String>();
        links.add(link(null, "first"));
        if (!janela.isEmpty()) {
            // Para trás, o hasNext diz se há mais itens antes; para a frente, só a primeira página não tem anterior.
            var haAnterior = pedida.scrollsBackward() ? janela.hasNext() : !pedida.isInitial();
            // Para trás, sempre há próxima: a janela terminou antes do item de onde a requisição partiu.
            var haProximo = pedida.scrollsBackward() || janela.hasNext();
            if (haAnterior) {
                links.add(link(cursor.apply(chaves(janela, 0), true), "prev"));
            }
            if (haProximo) {
                links.add(link(cursor.apply(chaves(janela, janela.size() - 1), false), "next"));
            }
        }
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.LINK, String.join(", ", links));
        return headers;
    }

    private static Map<String, ?> chaves(Window<?> janela, int indice) {
        return ((KeysetScrollPosition) janela.positionAt(indice)).getKeys();
    }

    private static String link(@Nullable String cursor, String relacao) {
        var uri = ServletUriComponentsBuilder.fromCurrentRequest();
        if (cursor == null) {
            uri.replaceQueryParam("cursor");
        } else {
            uri.replaceQueryParam("cursor", cursor);
        }
        return "<" + uri.build().toUriString() + ">; rel=\"" + relacao + "\"";
    }
}
