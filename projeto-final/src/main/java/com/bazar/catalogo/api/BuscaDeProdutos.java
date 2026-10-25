package com.bazar.catalogo.api;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import com.bazar.catalogo.dominio.FiltroDeProdutos;
import com.bazar.catalogo.dominio.StatusProduto;
import com.bazar.compartilhado.erros.ParametroDesconhecidoException;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * O corpo de POST /v1/produtos/buscas (13.01): os mesmos critérios da listagem, mais os que não cabem bem
 * numa URL, como os atributos ({"autor": "Robert C. Martin"}). Os nomes são os mesmos dos parâmetros de GET,
 * no plural quando o valor é uma lista.
 */
@IntervaloPrecoValido
public record BuscaDeProdutos(
        @Nullable @Size(min = 2, max = 100) String q,
        @Nullable @Sku String sku,
        @Nullable @Size(max = 100) List<UUID> categoriaIds,
        @Nullable List<StatusProduto> status,
        @Nullable @PositiveOrZero Long precoMin,
        @Nullable @PositiveOrZero Long precoMax,
        @Nullable Instant criadoApos,
        // Até 5 atributos; a chave em camelCase, como os nomes do contrato (12.02).
        @Nullable @Size(max = 5)
        Map<@Pattern(regexp = "[a-z][a-zA-Z]{0,29}") String, @NotBlank @Size(max = 100) String> atributos)
        implements IntervaloDePreco {

    public BuscaDeProdutos {
        q = FiltroProduto.semTextoVazio(q);
        sku = FiltroProduto.semTextoVazio(sku);
    }

    private static final SortedSet<String> CAMPOS = Arrays.stream(BuscaDeProdutos.class.getRecordComponents())
            .map(RecordComponent::getName)
            .collect(Collectors.toCollection(TreeSet::new));

    // Um critério com o nome errado não pode ser ignorado em silêncio: a busca devolveria mais produtos do que o
    // cliente pediu. O Jackson chama este método para cada campo do JSON que não é componente do record.
    @JsonAnySetter
    void recusarCampoDesconhecido(String campo, @Nullable Object valor) {
        throw new ParametroDesconhecidoException(new TreeSet<>(Set.of(campo)), CAMPOS);
    }

    FiltroDeProdutos paraDominio() {
        return new FiltroDeProdutos(q, sku, FiltroProduto.conjunto(categoriaIds), FiltroProduto.conjunto(status),
                IntervaloDePreco.emReais(precoMin), IntervaloDePreco.emReais(precoMax), criadoApos,
                atributos == null ? Map.of() : atributos);
    }
}
