package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * O If-Match de uma escrita não bate com o ETag atual do recurso (20.03): 412. Nada foi alterado.
 */
public class PrecondicaoFalhouException extends RegraDeNegocioException {

    public PrecondicaoFalhouException() {
        super(TipoProblema.PRECONDICAO_FALHOU, Map.of());
    }
}
