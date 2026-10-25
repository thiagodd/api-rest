package com.bazar.catalogo.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.bazar.catalogo.dominio.FiltroDeProdutos;
import com.bazar.catalogo.dominio.StatusProduto;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Os parâmetros de query de GET /v1/produtos (13.01). Os componentes do record são a lista de parâmetros aceitos:
 * qualquer outro nome recebe 400 (12.02). Parâmetros diferentes se somam (E); os valores de uma lista são
 * alternativas (OU), separados por vírgula ou com o parâmetro repetido. Os preços são em centavos, como nas respostas.
 */
@IntervaloPrecoValido
public record FiltroProduto(
        @Nullable @Size(min = 2, max = 100) String q,
        @Nullable @Sku String sku,
        @Nullable @Size(max = 20) List<UUID> categoriaId,
        @Nullable List<StatusProduto> status,
        @Nullable @PositiveOrZero Long precoMin,
        @Nullable @PositiveOrZero Long precoMax,
        @Nullable Instant criadoApos) implements IntervaloDePreco {

    // ?q= vazio (a caixa de busca em branco) é o mesmo que não informar o parâmetro, como já acontece com
    // ?categoriaId= e ?status=, que o Spring converte em null.
    public FiltroProduto {
        q = semTextoVazio(q);
        sku = semTextoVazio(sku);
    }

    FiltroDeProdutos paraDominio() {
        return new FiltroDeProdutos(q, sku, conjunto(categoriaId), conjunto(status),
                IntervaloDePreco.emReais(precoMin), IntervaloDePreco.emReais(precoMax), criadoApos, Map.of());
    }

    static @Nullable String semTextoVazio(@Nullable String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }

    static <T> Set<T> conjunto(@Nullable List<T> valores) {
        return valores == null ? Set.of() : Set.copyOf(valores);
    }
}
