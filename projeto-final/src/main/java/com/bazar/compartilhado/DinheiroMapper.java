package com.bazar.compartilhado;

import org.mapstruct.Mapper;

/**
 * Conversões de Dinheiro reaproveitadas pelos outros mappers (uses = DinheiroMapper.class).
 * Na v1, os preços saem em centavos de BRL.
 */
@Mapper(config = ConfiguracaoMapStruct.class)
public interface DinheiroMapper {

    default long emCentavos(Dinheiro dinheiro) {
        return dinheiro.emCentavos();
    }

    default Dinheiro deCentavosEmReais(Long centavos) {
        return Dinheiro.deCentavos(centavos, "BRL");
    }
}
