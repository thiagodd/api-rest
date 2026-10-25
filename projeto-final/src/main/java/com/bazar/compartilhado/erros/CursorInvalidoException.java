package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * O parâmetro cursor não foi emitido por esta API para esta consulta (400): foi alterado, montado à mão, veio de
 * outra versão do formato ou de outra consulta (13.04). O cliente recomeça a listagem sem o cursor.
 */
public class CursorInvalidoException extends ExcecaoDeDominio {

    public CursorInvalidoException() {
        super(TipoProblema.CURSOR_INVALIDO, Map.of());
    }
}
