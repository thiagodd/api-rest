package com.bazar.compartilhado.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um endpoint cujas chamadas devem ser registradas na auditoria (quem, o quê, quando, com que resultado).
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditavel {

    /** Nome da ação no registro de auditoria, como "categoria.remover". */
    String value();
}
