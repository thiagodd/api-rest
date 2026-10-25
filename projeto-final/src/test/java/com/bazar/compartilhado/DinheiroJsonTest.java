package com.bazar.compartilhado;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.DatabindException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

// A leitura do Dinheiro (15.02), com o Jackson configurado pelo Spring Boot.
@JsonTest
class DinheiroJsonTest {

    @Autowired
    JacksonTester<Dinheiro> json;

    @Test
    void leOValorEmString() throws Exception {
        assertThat(json.parseObject("""
                { "valor": "129.90", "moeda": "BRL" }
                """)).isEqualTo(Dinheiro.de("129.90", "BRL"));
    }

    // Um número também vale, lido como BigDecimal a partir do texto: um double perderia os centavos deste valor.
    @Test
    void leUmNumeroSemPassarPorDouble() throws Exception {
        assertThat((double) 12345678901234567.89).isEqualTo(12345678901234568.0);
        assertThat(json.parseObject("""
                { "valor": 12345678901234567.89, "moeda": "BRL" }
                """).valor()).hasToString("12345678901234567.89");
    }

    @Test
    void recusaMoedaDesconhecidaECasasDemais() {
        assertThatExceptionOfType(DatabindException.class).isThrownBy(() -> json.parseObject("""
                { "valor": "10.00", "moeda": "REAL" }
                """));
        assertThatExceptionOfType(DatabindException.class).isThrownBy(() -> json.parseObject("""
                { "valor": "10.999", "moeda": "BRL" }
                """));
        assertThatExceptionOfType(DatabindException.class).isThrownBy(() -> json.parseObject("""
                { "valor": "10.00" }
                """));
    }
}
