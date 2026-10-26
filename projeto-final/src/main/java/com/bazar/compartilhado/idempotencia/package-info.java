/**
 * Idempotency keys (21.01): repetir um POST com a mesma chave devolve a resposta da primeira vez, sem repetir o
 * efeito.
 */
@NullMarked
package com.bazar.compartilhado.idempotencia;

import org.jspecify.annotations.NullMarked;
