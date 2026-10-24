package com.bazar.compartilhado.validacao;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfValidatorTest {

    private final CpfValidator validador = new CpfValidator();

    @ParameterizedTest
    @ValueSource(strings = { "52998224725", "11144477735" })
    void aceitaCpfsComDigitosVerificadoresCorretos(String cpf) {
        assertThat(validador.isValid(cpf, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "52998224724", "529.982.247-25", "5299822472", "11111111111", "abcdefghijk" })
    void recusaCpfsInvalidos(String cpf) {
        assertThat(validador.isValid(cpf, null)).isFalse();
    }

    @org.junit.jupiter.api.Test
    void deixaONullParaONotBlank() {
        assertThat(validador.isValid(null, null)).isTrue();
    }
}
