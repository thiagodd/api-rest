package com.bazar.compartilhado.erros;

import java.util.Arrays;
import java.util.Map;

/**
 * Base das exceções que o domínio lança para o cliente. Não conhece HTTP: o status é decidido pelo
 * TratadorGlobalDeErros, a partir da subclasse.
 */
public abstract class ExcecaoDeDominio extends RuntimeException {

    private final String tipo;
    private final Object[] argumentos;
    private final Map<String, Object> extensoes;

    /**
     * @param tipo o final da URI do type (produto-nao-encontrado); também é a chave das mensagens
     * @param extensoes campos extras do Problem Details, para máquinas
     * @param argumentos valores para a mensagem do detail ({0}, {1}...)
     */
    protected ExcecaoDeDominio(String tipo, Map<String, Object> extensoes, Object... argumentos) {
        super(tipo + " " + Arrays.toString(argumentos));
        this.tipo = tipo;
        this.argumentos = argumentos;
        this.extensoes = Map.copyOf(extensoes);
    }

    public String getTipo() {
        return tipo;
    }

    public Object[] getArgumentos() {
        return argumentos.clone();
    }

    public Map<String, Object> getExtensoes() {
        return extensoes;
    }
}
