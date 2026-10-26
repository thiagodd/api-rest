package com.bazar.catalogo.api;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

/**
 * O ETag de uma representação a partir das versões (@Version) das entidades que ela mostra (20.02). É um ETag
 * forte: a mesma versão gera sempre os mesmos bytes. Calculá-lo não exige montar nem serializar a resposta, ao
 * contrário de um hash do corpo.
 */
final class VersaoDaRepresentacao {

    private VersaoDaRepresentacao() {
    }

    /** {@code "3"} para uma entidade na versão 3; {@code "3-7"} para duas. */
    static String etag(@Nullable Long... versoes) {
        return "\"" + Arrays.stream(versoes).map(versao -> String.valueOf(Objects.requireNonNull(versao)))
                .collect(Collectors.joining("-")) + "\"";
    }
}
