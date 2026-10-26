package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * Uma escrita que exige If-Match chegou sem ele (20.03): 428, como define a RFC 6585.
 */
public class PrecondicaoObrigatoriaException extends RegraDeNegocioException {

    public PrecondicaoObrigatoriaException() {
        super(TipoProblema.PRECONDICAO_OBRIGATORIA, Map.of());
    }
}
