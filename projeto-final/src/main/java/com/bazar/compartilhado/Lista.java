package com.bazar.compartilhado;

import java.util.List;

/**
 * Envelope das coleções: o Bazar nunca devolve um array no topo do JSON (02.06).
 */
public record Lista<T>(List<T> itens) {
}
