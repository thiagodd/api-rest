package com.bazar.relatorios.api;

import java.time.Instant;

import com.bazar.relatorios.dominio.PeriodoDeVendas;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * Os pedidos que entram no relatório: criados a partir de criadoApos e antes de criadoAntes, os mesmos nomes do filtro
 * da listagem de pedidos (BZR-NOM-05). O período é semiaberto, [criadoApos, criadoAntes).
 */
public record SolicitarRelatorioVendasRequest(@NotNull @Nullable Instant criadoApos,
        @NotNull @Nullable Instant criadoAntes) {

    // A validação aparece em erros[] com o campo "periodo".
    @AssertTrue(message = "{bazar.validacao.periodo.message}")
    boolean isPeriodo() {
        return criadoApos == null || criadoAntes == null || criadoApos.isBefore(criadoAntes);
    }

    PeriodoDeVendas paraPeriodo() {
        return new PeriodoDeVendas(criadoApos, criadoAntes);
    }
}
