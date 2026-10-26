package com.bazar.compartilhado.idempotencia;

import java.util.Map;

/**
 * O que é preciso para repetir uma resposta: o status, os headers que importam ao cliente e o corpo, em bytes. O
 * hash da requisição separa uma repetição legítima de uma chave reutilizada com outro corpo.
 */
record RespostaGuardada(String hashRequisicao, int status, Map<String, String> headers, byte[] corpo) {
}
