package com.bazar.catalogo.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

/**
 * Constraint de classe: precoMin não pode ser maior que precoMax. O erro é atribuído ao campo precoMax.
 */
@Documented
@Constraint(validatedBy = IntervaloPrecoValido.Validador.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntervaloPrecoValido {

    String message() default "{bazar.validacao.IntervaloPrecoValido.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validador implements ConstraintValidator<IntervaloPrecoValido, IntervaloPreco> {

        @Override
        public boolean isValid(IntervaloPreco intervalo, ConstraintValidatorContext contexto) {
            if (intervalo.precoMin() == null || intervalo.precoMax() == null
                    || intervalo.precoMin() <= intervalo.precoMax()) {
                return true;
            }
            contexto.disableDefaultConstraintViolation();
            contexto.buildConstraintViolationWithTemplate(contexto.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("precoMax")
                    .addConstraintViolation();
            return false;
        }
    }
}
