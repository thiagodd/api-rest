package com.bazar.compartilhado.web;

import java.util.List;

import org.springframework.http.ETag;

/**
 * O If-Match de uma escrita, já interpretado (20.03). A comparação é forte (RFC 9110, §13.1.1): um ETag fraco
 * (W/"3") nunca bate, e o * bate com qualquer versão de um recurso que existe.
 */
public record PrecondicaoIfMatch(List<ETag> etags) {

    public static PrecondicaoIfMatch ler(String ifMatch) {
        return new PrecondicaoIfMatch(ETag.parse(ifMatch));
    }

    public boolean aceita(String etagAtual) {
        var atual = ETag.create(etagAtual);
        return etags.stream().anyMatch(etag -> etag.isWildcard() || etag.compare(atual, true));
    }
}
