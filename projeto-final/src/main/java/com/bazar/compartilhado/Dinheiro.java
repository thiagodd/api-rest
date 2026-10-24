package com.bazar.compartilhado;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Um valor monetário com a sua moeda. Nunca use double para dinheiro (02.06).
 * Esta é a versão mínima; a validação e a serialização completas estão em 15.02.
 */
public record Dinheiro(BigDecimal valor, Currency moeda) {

    public static Dinheiro deCentavos(long centavos, String codigoMoeda) {
        var moeda = Currency.getInstance(codigoMoeda);
        return new Dinheiro(BigDecimal.valueOf(centavos, moeda.getDefaultFractionDigits()), moeda);
    }

    public long emCentavos() {
        return valor.movePointRight(moeda.getDefaultFractionDigits()).longValueExact();
    }
}
