package com.bazar.compartilhado.operacoes;

import java.util.UUID;

/**
 * Onde fica o resultado de um tipo de operação (21.02). Cada módulo que cria operações registra o seu: o
 * OperacaoController não conhece relatórios nem reembolsos.
 */
public interface LocalDoResultado {

    String tipo();

    /** O caminho do recurso com o resultado, como /v1/relatorios/{id}. */
    String caminho(UUID operacao);
}
