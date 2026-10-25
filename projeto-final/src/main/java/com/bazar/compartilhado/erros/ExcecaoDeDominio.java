package com.bazar.compartilhado.erros;

import java.util.Arrays;
import java.util.Map;

/**
 * Base das exceções que o domínio lança para o cliente. Cada uma aponta um tipo do catálogo (TipoProblema, 16.01),
 * que define o status e a URI do type.
 */
public abstract class ExcecaoDeDominio extends RuntimeException {

    private final TipoProblema tipo;
    private final Object[] argumentos;
    private final Map<String, Object> extensoes;

    /**
     * @param tipo o tipo do catálogo; o slug dele (produto-nao-encontrado) também é a chave das mensagens
     * @param extensoes campos extras do Problem Details, para máquinas
     * @param argumentos valores para a mensagem do detail ({0}, {1}...)
     */
    protected ExcecaoDeDominio(TipoProblema tipo, Map<String, Object> extensoes, Object... argumentos) {
        super(tipo + " " + Arrays.toString(argumentos));
        this.tipo = tipo;
        this.argumentos = argumentos;
        this.extensoes = Map.copyOf(extensoes);
    }

    public TipoProblema getTipo() {
        return tipo;
    }

    public Object[] getArgumentos() {
        return argumentos.clone();
    }

    public Map<String, Object> getExtensoes() {
        return extensoes;
    }
}
