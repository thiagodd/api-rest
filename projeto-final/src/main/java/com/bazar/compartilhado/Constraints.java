package com.bazar.compartilhado;

import org.hibernate.exception.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Identifica qual constraint do banco uma escrita violou (14.01). O nome vem do PostgreSQL (produto_sku_key é o
 * nome que ele dá ao unique da coluna sku) e chega pela exceção do Hibernate, dentro da exceção do Spring.
 */
public final class Constraints {

    private Constraints() {
    }

    public static boolean violou(DataIntegrityViolationException ex, String constraint) {
        for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
            if (causa instanceof ConstraintViolationException violacao) {
                return constraint.equalsIgnoreCase(violacao.getConstraintName());
            }
        }
        return false;
    }
}
