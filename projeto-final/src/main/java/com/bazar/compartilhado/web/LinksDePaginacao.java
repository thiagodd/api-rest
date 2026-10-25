package com.bazar.compartilhado.web;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * O header Link (RFC 8288) de uma coleção paginada por offset (13.03): first, prev, next e last. Cada URI é a
 * requisição atual com outro page; os demais parâmetros (filtros, limit) são preservados.
 */
public final class LinksDePaginacao {

    private LinksDePaginacao() {
    }

    public static HttpHeaders de(Page<?> pagina) {
        var links = new ArrayList<String>();
        var atual = pagina.getNumber() + 1;
        var ultima = Math.max(pagina.getTotalPages(), 1);
        links.add(link(1, "first"));
        if (atual > 1) {
            links.add(link(Math.min(atual - 1, ultima), "prev"));
        }
        if (atual < ultima) {
            links.add(link(atual + 1, "next"));
        }
        links.add(link(ultima, "last"));
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.LINK, String.join(", ", links));
        return headers;
    }

    private static String link(int pagina, String relacao) {
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("page", pagina)
                .build()
                .toUriString();
        return "<" + uri + ">; rel=\"" + relacao + "\"";
    }
}
