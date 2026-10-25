package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * A requisição é válida, mas o estado atual não permite a operação (409).
 */
public class RegraDeNegocioException extends ExcecaoDeDominio {

    public RegraDeNegocioException(TipoProblema tipo, Map<String, Object> extensoes, Object... argumentos) {
        super(tipo, extensoes, argumentos);
    }
}
