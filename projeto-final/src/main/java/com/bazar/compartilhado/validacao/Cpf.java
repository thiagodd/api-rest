package com.bazar.compartilhado.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * CPF com 11 dígitos (sem pontuação) e dígitos verificadores corretos. null é válido.
 */
@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Cpf {

    String message() default "{bazar.validacao.Cpf.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
