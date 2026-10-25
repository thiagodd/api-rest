package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * O cliente alterou um recurso a partir de uma versão que já não é a atual (10.06). Em 20.03, a versão
 * passa a viajar no ETag e no If-Match.
 */
public class ConflitoDeVersaoException extends RegraDeNegocioException {

    public ConflitoDeVersaoException(long versaoAtual) {
        super("conflito-de-versao", Map.of("versaoAtual", versaoAtual));
    }

    // Quando o conflito é detectado no UPDATE (@Version), a versão nova não é conhecida.
    public ConflitoDeVersaoException() {
        super("conflito-de-versao", Map.of());
    }
}
