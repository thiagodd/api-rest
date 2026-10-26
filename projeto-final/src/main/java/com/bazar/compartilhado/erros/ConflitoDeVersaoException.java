package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * O cliente alterou um recurso a partir de uma versão que já não é a atual (10.06). Com a versão no If-Match (20.03),
 * a camada web traduz este conflito em 412 (PrecondicaoFalhouException).
 */
public class ConflitoDeVersaoException extends RegraDeNegocioException {

    public ConflitoDeVersaoException(long versaoAtual) {
        super(TipoProblema.CONFLITO_DE_VERSAO, Map.of("versaoAtual", versaoAtual));
    }

    // Quando o conflito é detectado no UPDATE (@Version), a versão nova não é conhecida.
    public ConflitoDeVersaoException() {
        super(TipoProblema.CONFLITO_DE_VERSAO, Map.of());
    }
}
