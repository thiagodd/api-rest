package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * O recurso pedido não existe (404).
 */
public class RecursoNaoEncontradoException extends ExcecaoDeDominio {

    public RecursoNaoEncontradoException(String tipo, Object id) {
        super(tipo, Map.of(), id);
    }
}
