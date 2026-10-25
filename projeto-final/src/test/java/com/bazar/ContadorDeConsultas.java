package com.bazar;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

/**
 * Conta os comandos SQL que o Hibernate preparou durante um trecho de código (10.04).
 * Exige spring.jpa.properties.hibernate.generate_statistics=true no teste.
 */
public final class ContadorDeConsultas {

    private final Statistics estatisticas;

    public ContadorDeConsultas(EntityManagerFactory emf) {
        this.estatisticas = emf.unwrap(SessionFactory.class).getStatistics();
    }

    public long contar(Runnable trecho) {
        estatisticas.clear();
        trecho.run();
        return estatisticas.getPrepareStatementCount();
    }
}
