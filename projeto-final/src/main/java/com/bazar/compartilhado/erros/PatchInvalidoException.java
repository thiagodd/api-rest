package com.bazar.compartilhado.erros;

import java.util.Map;

/**
 * Um documento JSON Patch malformado (400, RFC 5789 §2.2): não é uma lista, ou uma operação não tem op e path
 * válidos (14.02). O corpo traz a posição da operação na lista, a partir de 0.
 */
public class PatchInvalidoException extends ExcecaoDeDominio {

    public PatchInvalidoException(int operacao) {
        super("patch-invalido", Map.of("operacao", operacao), operacao);
    }
}
