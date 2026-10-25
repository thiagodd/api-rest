package com.bazar.compartilhado;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdsTest {

    @Test
    void geraUuidVersao7ComOInstanteNosPrimeirosBits() {
        var antes = System.currentTimeMillis();
        var id = Ids.novo();
        var depois = System.currentTimeMillis();

        assertThat(id.version()).isEqualTo(7);
        assertThat(id.variant()).isEqualTo(2);
        // Os 48 bits mais significativos são os milissegundos desde 1970.
        assertThat(id.getMostSignificantBits() >>> 16).isBetween(antes, depois);
    }

    // Gerados em sequência (vários no mesmo milissegundo), os ids já saem em ordem, como texto e como UUID.
    @Test
    void idsGeradosEmSequenciaJaEstaoOrdenados() {
        var ids = new ArrayList<UUID>();
        IntStream.range(0, 10_000).forEach(i -> ids.add(Ids.novo()));

        assertThat(ids).isSortedAccordingTo(Comparator.naturalOrder());
        assertThat(ids.stream().map(UUID::toString).toList()).isSorted();
        assertThat(ids).doesNotHaveDuplicates();
    }
}
