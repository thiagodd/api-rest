package com.bazar.catalogo.dominio;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;

import org.springframework.data.domain.Sort;

/**
 * Projeção de leitura para listagens (10.05): só as colunas que a lista mostra, sem descrição nem atributos.
 * Os nomes e tipos dos componentes precisam bater com as propriedades de Produto. O criadoEm não sai na
 * resposta, mas é uma das chaves da ordenação, e o cursor precisa dele (13.04).
 */
public record ProdutoResumo(UUID id, String sku, String nome, Dinheiro preco, StatusProduto status, UUID categoriaId,
        Instant criadoEm) {

    /**
     * Os valores deste produto nas propriedades da ordenação, na mesma ordem: a posição dele na listagem (13.04).
     */
    public Map<String, Object> chaves(Sort ordenacao) {
        var chaves = new LinkedHashMap<String, Object>();
        for (var ordem : ordenacao) {
            chaves.put(ordem.getProperty(), switch (ordem.getProperty()) {
                case "id" -> id;
                case "sku" -> sku;
                case "nome" -> nome;
                case "preco.valor" -> preco.valor();
                case "criadoEm" -> criadoEm;
                default -> throw new IllegalArgumentException("Propriedade sem valor no resumo: " + ordem.getProperty());
            });
        }
        return chaves;
    }
}
