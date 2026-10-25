package com.bazar.catalogo.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Os filtros da listagem e da busca de produtos (13.01). Um campo nulo ou vazio não filtra; os que vêm
 * preenchidos se somam (E). Dentro de um conjunto (categoriaIds, status), basta um valor coincidir (OU).
 */
public record FiltroDeProdutos(
        @Nullable String texto,
        @Nullable String sku,
        Set<UUID> categoriaIds,
        Set<StatusProduto> status,
        @Nullable BigDecimal precoMinimo,
        @Nullable BigDecimal precoMaximo,
        @Nullable Instant criadoApos,
        Map<String, String> atributos) {

    public FiltroDeProdutos {
        categoriaIds = Set.copyOf(categoriaIds);
        status = Set.copyOf(status);
        atributos = Map.copyOf(atributos);
    }

    public static FiltroDeProdutos nenhum() {
        return new FiltroDeProdutos(null, null, Set.of(), Set.of(), null, null, null, Map.of());
    }

    public FiltroDeProdutos comTexto(String texto) {
        return new FiltroDeProdutos(texto, sku, categoriaIds, status, precoMinimo, precoMaximo, criadoApos, atributos);
    }

    public FiltroDeProdutos daCategoria(UUID... categoriaIds) {
        return new FiltroDeProdutos(texto, sku, Set.of(categoriaIds), status, precoMinimo, precoMaximo, criadoApos,
                atributos);
    }

    public FiltroDeProdutos comStatus(StatusProduto... status) {
        return new FiltroDeProdutos(texto, sku, categoriaIds, Set.of(status), precoMinimo, precoMaximo, criadoApos,
                atributos);
    }

    public FiltroDeProdutos comAtributo(String chave, String valor) {
        var novos = new HashMap<>(atributos);
        novos.put(chave, valor);
        return new FiltroDeProdutos(texto, sku, categoriaIds, status, precoMinimo, precoMaximo, criadoApos, novos);
    }

    public FiltroDeProdutos custando(@Nullable BigDecimal minimo, @Nullable BigDecimal maximo) {
        return new FiltroDeProdutos(texto, sku, categoriaIds, status, minimo, maximo, criadoApos, atributos);
    }
}
