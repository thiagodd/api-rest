package com.bazar.compartilhado;

import org.springframework.boot.jackson.JacksonComponent;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Formato JSON do Dinheiro: { "valor": "129.90", "moeda": "BRL" }.
 * O valor vai como string para não perder precisão em clientes que leem números como double (02.06).
 * A leitura (desserialização) chega em 15.02.
 */
@JacksonComponent
public class DinheiroJson {

    public static class Serializer extends ValueSerializer<Dinheiro> {

        @Override
        public void serialize(Dinheiro dinheiro, JsonGenerator gerador, SerializationContext contexto) {
            gerador.writeStartObject();
            gerador.writeStringProperty("valor", dinheiro.valor().toPlainString());
            gerador.writeStringProperty("moeda", dinheiro.moeda().getCurrencyCode());
            gerador.writeEndObject();
        }
    }
}
