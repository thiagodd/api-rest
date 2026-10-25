package com.bazar.compartilhado;

import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

/**
 * Os ids do Bazar são UUID v7 (RFC 9562, 15.03): os primeiros 48 bits são o instante da criação em milissegundos,
 * e o resto é aleatório. Ordenados pelo tempo, eles entram no fim do índice B-tree, como um id sequencial, sem
 * revelar quantos pedidos o Bazar tem.
 * <p>
 * O JDK 25 só gera UUID v4 (randomUUID). A partir do JDK 26, UUID.ofEpochMillis(System.currentTimeMillis())
 * substitui a biblioteca nesta linha.
 */
public final class Ids {

    private Ids() {
    }

    public static UUID novo() {
        // Dentro do mesmo milissegundo, um contador mantém a ordem de criação.
        return UuidCreator.getTimeOrderedEpoch();
    }
}
