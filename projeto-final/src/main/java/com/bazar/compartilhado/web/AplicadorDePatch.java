package com.bazar.compartilhado.web;

import java.io.StringReader;
import java.util.Set;

import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.PatchInvalidoException;
import com.bazar.compartilhado.erros.TesteDoPatchFalhouException;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonStructure;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import org.springframework.stereotype.Component;

/**
 * Aplica um patch à parte editável de um recurso e devolve o resultado validado (14.02). O recurso vira JSON, o
 * patch é aplicado ao JSON, e o resultado volta para o record, que passa pelo Bean Validation como um PUT.
 *
 * <ul>
 * <li>JSON Merge Patch (RFC 7396): implementado aqui, sobre o JsonNode do Jackson 3.</li>
 * <li>JSON Patch (RFC 6902): aplicado pela Jakarta JSON Processing (Parsson), uma operação por vez, para
 * distinguir um test que falhou (409) de uma operação impossível (422).</li>
 * </ul>
 */
@Component
public class AplicadorDePatch {

    private static final Set<String> OPERACOES = Set.of("add", "remove", "replace", "move", "copy", "test");

    private final JsonMapper json;
    private final Validator validador;

    public AplicadorDePatch(JsonMapper json, Validator validador) {
        this.json = json;
        this.validador = validador;
    }

    public <T> T mergePatch(T atual, JsonNode patch, Class<T> tipo) {
        return validado(mesclar(json.valueToTree(atual), patch), tipo);
    }

    // O algoritmo da RFC 7396, §2: null remove o campo; um objeto é mesclado campo a campo; qualquer outro valor
    // (inclusive uma lista) substitui o que havia.
    static JsonNode mesclar(JsonNode alvo, JsonNode patch) {
        if (!patch.isObject()) {
            return patch;
        }
        var resultado = alvo.isObject() ? ((ObjectNode) alvo).deepCopy() : JsonMapper.shared().createObjectNode();
        for (var campo : patch.properties()) {
            if (campo.getValue().isNull()) {
                resultado.remove(campo.getKey());
            } else {
                var atual = resultado.path(campo.getKey());
                resultado.set(campo.getKey(), mesclar(atual, campo.getValue()));
            }
        }
        return resultado;
    }

    public <T> T jsonPatch(T atual, JsonNode operacoes, Class<T> tipo) {
        if (!operacoes.isArray()) {
            throw new PatchInvalidoException(0);
        }
        JsonStructure documento = paraJsonP(json.valueToTree(atual));
        for (var i = 0; i < operacoes.size(); i++) {
            var operacao = operacoes.get(i);
            if (!operacao.isObject() || !OPERACOES.contains(operacao.path("op").asString(""))
                    || !operacao.path("path").isString()) {
                throw new PatchInvalidoException(i);
            }
            try {
                documento = Json.createPatch(Json.createArrayBuilder().add(paraJsonP(operacao)).build())
                        .apply(documento);
            } catch (JsonException ex) {
                var caminho = operacao.path("path").asString();
                if ("test".equals(operacao.path("op").asString())) {
                    throw new TesteDoPatchFalhouException(i, caminho);
                }
                throw new CampoInvalidoException(caminho, "bazar.patch.operacao-impossivel");
            }
        }
        return validado(json.readTree(documento.toString()), tipo);
    }

    // Um campo que o record não tem (sku, status, id) não é editável: 422, em vez de ser ignorado em silêncio.
    private <T> T validado(JsonNode documento, Class<T> tipo) {
        T resultado;
        try {
            resultado = json.readerFor(tipo)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(documento);
        } catch (DatabindException ex) {
            var campo = ex.getPath().isEmpty() ? "" : ex.getPath().getLast().getPropertyName();
            throw new CampoInvalidoException(campo, "bazar.patch.campo-invalido");
        }
        var violacoes = validador.validate(resultado);
        if (!violacoes.isEmpty()) {
            throw new ConstraintViolationException(violacoes);
        }
        return resultado;
    }

    private static JsonStructure paraJsonP(JsonNode no) {
        try (var leitor = Json.createReader(new StringReader(no.toString()))) {
            return leitor.read();
        }
    }
}
