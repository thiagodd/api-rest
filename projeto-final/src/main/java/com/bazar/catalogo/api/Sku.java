package com.bazar.catalogo.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

/**
 * SKU do Bazar: três letras maiúsculas, hífen e quatro dígitos (LIV-0042). null é válido.
 * Constraint composta: reaproveita @Pattern e não precisa de validador próprio.
 */
@Documented
@Pattern(regexp = "[A-Z]{3}-\\d{4}")
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Sku {

    String message() default "{bazar.validacao.Sku.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
