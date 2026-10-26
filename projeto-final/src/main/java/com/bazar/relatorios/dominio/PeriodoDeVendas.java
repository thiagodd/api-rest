package com.bazar.relatorios.dominio;

import java.time.Instant;

/**
 * O período de um relatório de vendas, semiaberto: [de, ate).
 */
public record PeriodoDeVendas(Instant de, Instant ate) {
}
