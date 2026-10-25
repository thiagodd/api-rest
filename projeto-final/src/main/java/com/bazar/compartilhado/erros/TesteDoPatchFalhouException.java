package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * Uma operação test de um JSON Patch não confere com o estado atual do recurso (409, RFC 5789 §2.2: o estado
 * atual não permite a mudança). Nada foi alterado: o patch é atômico (14.02).
 */
public class TesteDoPatchFalhouException extends ExcecaoDeDominio {

    public TesteDoPatchFalhouException(int operacao, String caminho) {
        super(TipoProblema.TESTE_DO_PATCH_FALHOU, Map.of("operacao", operacao, "path", caminho), caminho);
    }
}
