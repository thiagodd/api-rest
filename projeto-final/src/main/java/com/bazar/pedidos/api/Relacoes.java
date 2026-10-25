package com.bazar.pedidos.api;

import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;

/**
 * As relações próprias do Bazar. Pela RFC 8288, uma relação que não é registrada na IANA deve ser uma URI;
 * no HAL, a CURIE "bazar" abrevia https://api.bazar.example/rels/{rel} (03.04).
 */
final class Relacoes {

    static final LinkRelation CANCELAR = HalLinkRelation.curied("bazar", "cancelar");

    private Relacoes() {
    }
}
