package com.bazar.compartilhado.idempotencia;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um endpoint que aceita o header Idempotency-Key (21.01). Quem processa a chave é o IdempotenciaFilter; o
 * endpoint não muda nada.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotente {
}
