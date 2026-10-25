package com.bazar.catalogo.api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import com.bazar.catalogo.dominio.FiltroDeProdutos;
import com.bazar.compartilhado.erros.CursorInvalidoException;
import com.bazar.compartilhado.web.CodificadorDeCursor;
import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;

/**
 * A posição na listagem de produtos (13.04): os valores de um produto já visto em cada campo da ordenação e a
 * direção. A ordenação é escolhida pelo cliente (13.02), então o cursor guarda também uma impressão digital da
 * consulta (filtros e ordenação): os valores só fazem sentido na ordenação em que foram lidos.
 *
 * @param consulta o resumo (SHA-256) dos filtros e da ordenação
 * @param valores um valor, como texto, para cada propriedade da ordenação, na mesma ordem
 */
record CursorDeProdutos(String consulta, List<String> valores, boolean paraTras) {

    static KeysetScrollPosition posicao(@Nullable String cursor, FiltroDeProdutos filtro, Sort ordenacao) {
        if (cursor == null) {
            return ScrollPosition.keyset();
        }
        var lido = CodificadorDeCursor.decodificar(cursor, CursorDeProdutos.class);
        var propriedades = ordenacao.stream().map(Sort.Order::getProperty).toList();
        if (!lido.consulta().equals(consulta(filtro, ordenacao)) || lido.valores().size() != propriedades.size()) {
            throw new CursorInvalidoException();
        }
        var chaves = new LinkedHashMap<String, Object>();
        try {
            for (var i = 0; i < propriedades.size(); i++) {
                chaves.put(propriedades.get(i), valor(propriedades.get(i), lido.valores().get(i)));
            }
        } catch (RuntimeException ex) {
            throw new CursorInvalidoException();
        }
        return lido.paraTras() ? ScrollPosition.backward(chaves) : ScrollPosition.forward(chaves);
    }

    static String de(FiltroDeProdutos filtro, Sort ordenacao, Map<String, ?> chaves, boolean paraTras) {
        var valores = new ArrayList<String>();
        ordenacao.forEach(ordem -> valores.add(String.valueOf(chaves.get(ordem.getProperty()))));
        return CodificadorDeCursor.codificar(new CursorDeProdutos(consulta(filtro, ordenacao), valores, paraTras));
    }

    // O tipo de cada propriedade ordenável: o JSON do cursor só tem texto.
    private static Object valor(String propriedade, String texto) {
        return switch (propriedade) {
            case "id" -> UUID.fromString(texto);
            case "preco.valor" -> new BigDecimal(texto);
            case "criadoEm" -> Instant.parse(texto);
            case "nome", "sku" -> texto;
            default -> throw new IllegalArgumentException(propriedade);
        };
    }

    // Os conjuntos e o mapa entram ordenados: a mesma consulta, com os parâmetros em outra ordem, dá o mesmo resumo.
    private static String consulta(FiltroDeProdutos filtro, Sort ordenacao) {
        var texto = String.join("\n", String.valueOf(filtro.texto()), String.valueOf(filtro.sku()),
                new TreeSet<>(filtro.categoriaIds().stream().map(UUID::toString).toList()).toString(),
                new TreeSet<>(filtro.status()).toString(), String.valueOf(filtro.precoMinimo()),
                String.valueOf(filtro.precoMaximo()), String.valueOf(filtro.criadoApos()),
                new TreeMap<>(filtro.atributos()).toString(), ordenacao.toString());
        try {
            var resumo = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(resumo).substring(0, 16);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
