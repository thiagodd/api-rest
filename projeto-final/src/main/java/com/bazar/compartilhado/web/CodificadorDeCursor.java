package com.bazar.compartilhado.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.bazar.compartilhado.erros.CursorInvalidoException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Transforma a posição de uma listagem num cursor opaco e de volta (13.04): JSON em Base64 para URLs, sem o "=" do
 * final.
 */
public final class CodificadorDeCursor {

    // Um mapper próprio, e não o da API: o formato do cursor é interno e não deve mudar quando a configuração do
    // JSON das respostas muda. E estrito: um campo a mais ou a menos é um cursor que a API não emitiu.
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
            .build();

    private CodificadorDeCursor() {
    }

    public static String codificar(Object posicao) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(JSON.writeValueAsBytes(posicao));
    }

    /**
     * @throws CursorInvalidoException se o texto não é Base64, não é JSON ou não tem os campos do tipo
     */
    public static <T> T decodificar(String cursor, Class<T> tipo) {
        try {
            var json = Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.US_ASCII));
            return JSON.readValue(json, tipo);
        } catch (IllegalArgumentException | JacksonException ex) {
            throw new CursorInvalidoException();
        }
    }
}
