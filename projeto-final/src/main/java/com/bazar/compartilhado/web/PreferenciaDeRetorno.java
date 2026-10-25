package com.bazar.compartilhado.web;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * A preferência return do header Prefer (RFC 7240), na criação (14.01). Prefer: return=minimal pede uma resposta
 * sem corpo; o padrão do Bazar é devolver a representação. Preferências desconhecidas são ignoradas, como a RFC
 * exige, e return=minimal junto com return=representation vale como nenhuma das duas.
 */
public final class PreferenciaDeRetorno {

    public static final String APLICADA = "Preference-Applied";

    private PreferenciaDeRetorno() {
    }

    /**
     * @param prefer o valor do header, como "return=minimal" ou "respond-async, return=minimal; foo=bar"
     */
    public static boolean minima(@Nullable String prefer) {
        if (prefer == null) {
            return false;
        }
        var retornos = Arrays.stream(prefer.split(","))
                .map(preferencia -> preferencia.split(";")[0].strip().split("=", 2))
                // O nome da preferência não diferencia maiúsculas; o valor diferencia.
                .filter(par -> par.length == 2 && par[0].strip().equalsIgnoreCase("return"))
                .map(par -> par[1].strip())
                .distinct()
                .toList();
        return retornos.equals(List.of("minimal"));
    }
}
