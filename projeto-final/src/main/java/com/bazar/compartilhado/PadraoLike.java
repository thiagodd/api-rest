package com.bazar.compartilhado;

import java.util.Locale;

/**
 * O padrão de um LIKE "contém", em minúsculas, para comparar com lower(coluna) e usar com escape '\'.
 * Os curingas digitados pelo cliente (% e _) viram texto comum (10.05).
 */
public final class PadraoLike {

    private PadraoLike() {
    }

    public static String contendo(String texto) {
        return "%" + texto.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
