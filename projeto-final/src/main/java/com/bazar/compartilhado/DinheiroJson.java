package com.bazar.compartilhado;

import java.math.BigDecimal;
import java.util.Currency;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;

/**
 * Formato JSON do Dinheiro: { "valor": "129.90", "moeda": "BRL" } (15.02).
 * O valor sai como string para não perder precisão em clientes que leem números como double (02.06). Na leitura,
 * um número também é aceito: o Jackson o lê como BigDecimal, sem passar por double.
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

    // Qualquer problema (valor ausente, moeda que não é ISO 4217, casas demais) vira um erro de leitura do corpo:
    // 400, sem a mensagem interna. O valor é lido token a token: um número vira BigDecimal direto do texto, sem
    // passar por double (um readTree leria 12345678901234567.89 como double e perderia os centavos).
    public static class Deserializer extends ValueDeserializer<Dinheiro> {

        @Override
        public Dinheiro deserialize(JsonParser parser, DeserializationContext contexto) {
            if (!parser.isExpectedStartObjectToken()) {
                return contexto.reportInputMismatch(Dinheiro.class, "Dinheiro precisa ser um objeto");
            }
            BigDecimal valor = null;
            String moeda = null;
            for (var campo = parser.nextName(); campo != null; campo = parser.nextName()) {
                var token = parser.nextToken();
                switch (campo) {
                    case "valor" -> valor = switch (token) {
                        case VALUE_STRING -> decimal(parser.getString(), contexto);
                        case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> parser.getDecimalValue();
                        default -> null;
                    };
                    case "moeda" -> moeda = token == JsonToken.VALUE_STRING ? parser.getString() : null;
                    default -> parser.skipChildren();
                }
            }
            if (valor == null || moeda == null) {
                return contexto.reportInputMismatch(Dinheiro.class, "Dinheiro precisa de valor e moeda");
            }
            try {
                return new Dinheiro(valor, Currency.getInstance(moeda));
            } catch (IllegalArgumentException ex) {
                return contexto.reportInputMismatch(Dinheiro.class, ex.getMessage());
            }
        }

        private static BigDecimal decimal(String texto, DeserializationContext contexto) {
            try {
                return new BigDecimal(texto);
            } catch (NumberFormatException ex) {
                return contexto.reportInputMismatch(Dinheiro.class, "Valor não é um número: " + texto);
            }
        }
    }
}
