package com.bazar.compartilhado;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * Por onde o cliente chegou ao Bazar. Vem do cabeçalho Bazar-Canal (09.02).
 */
public enum Canal {
    WEB, APP, PARCEIRO, DESCONHECIDO;

    /** Valor ausente ou desconhecido vira DESCONHECIDO: o canal é informativo e não deve recusar requisições. */
    public static Canal de(@Nullable String valor) {
        if (valor == null) {
            return DESCONHECIDO;
        }
        try {
            return valueOf(valor.strip().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException e) {
            return DESCONHECIDO;
        }
    }
}
