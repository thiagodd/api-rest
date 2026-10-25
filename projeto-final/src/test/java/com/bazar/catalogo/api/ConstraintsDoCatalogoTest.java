package com.bazar.catalogo.api;

import java.util.Locale;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintsDoCatalogoTest {

    private final Validator validator = Validation.byProvider(HibernateValidator.class)
            .configure()
            .defaultLocale(Locale.forLanguageTag("pt-BR"))
            .buildValidatorFactory()
            .getValidator();

    record ComSku(@Sku String sku) {
    }

    @IntervaloPrecoValido
    record Intervalo(@Nullable Long precoMin, @Nullable Long precoMax) implements IntervaloDePreco {
    }

    @Test
    void skuForaDoFormatoGeraUmaUnicaViolacaoComAMensagemDoBazar() {
        var violacoes = validator.validate(new ComSku("liv-42"));

        assertThat(violacoes).extracting(ConstraintViolation::getMessage)
                .containsExactly("deve seguir o formato AAA-0000 (ex.: LIV-0042)");
    }

    @Test
    void skuNoFormatoENullSaoValidos() {
        assertThat(validator.validate(new ComSku("LIV-0042"))).isEmpty();
        assertThat(validator.validate(new ComSku(null))).isEmpty();
    }

    @Test
    void intervaloInvertidoAcusaOCampoPrecoMax() {
        var violacoes = validator.validate(new Intervalo(5000L, 1000L));

        assertThat(violacoes).singleElement().satisfies(v -> {
            assertThat(v.getPropertyPath()).hasToString("precoMax");
            assertThat(v.getMessage()).isEqualTo("deve ser maior ou igual a precoMin");
        });
    }

    @Test
    void intervaloComUmLimiteSoOuEmOrdemEValido() {
        assertThat(validator.validate(new Intervalo(1000L, 5000L))).isEmpty();
        assertThat(validator.validate(new Intervalo(null, 5000L))).isEmpty();
        assertThat(validator.validate(new Intervalo(1000L, null))).isEmpty();
    }
}
