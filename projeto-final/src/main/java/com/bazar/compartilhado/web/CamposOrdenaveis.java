package com.bazar.compartilhado.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.bazar.compartilhado.erros.OrdenacaoInvalidaException;

import org.springframework.data.domain.Sort;

/**
 * Os campos pelos quais uma coleção pode ser ordenada (13.02): o nome no contrato e a propriedade da entidade,
 * que nem sempre coincidem (precoEmCentavos → preco.valor). O Spring entrega o Sort com os nomes que o cliente
 * escreveu, sem validar nada; esta classe recusa o que não está na lista e traduz o resto.
 *
 * <p>Toda ordenação termina pelo id: sem um critério único no fim, dois itens com o mesmo valor podem trocar de
 * lugar entre duas consultas, e a paginação repete ou pula itens (13.03).
 *
 * @param propriedadePorCampo o nome no contrato → a propriedade da entidade
 * @param padrao a ordenação quando o cliente não pede nenhuma, já com os nomes do contrato
 */
public record CamposOrdenaveis(Map<String, String> propriedadePorCampo, Sort padrao) {

    public static final int MAXIMO_DE_CAMPOS = 3;

    private static final Sort DESEMPATE = Sort.by("id");

    public CamposOrdenaveis {
        propriedadePorCampo = Map.copyOf(propriedadePorCampo);
    }

    public Sort traduzir(Sort pedida) {
        var ordens = (pedida.isSorted() ? pedida : padrao).toList();
        var recusados = new ArrayList<String>();
        var vistos = new HashSet<String>();
        for (var ordem : ordens) {
            if (!propriedadePorCampo.containsKey(ordem.getProperty()) || !vistos.add(ordem.getProperty())) {
                recusados.add(ordem.getProperty());
            }
            // Ignorar maiúsculas exige lower() no ORDER BY, que nenhum índice comum atende.
            else if (ordem.isIgnoreCase()) {
                recusados.add(ordem.getProperty() + ",ignorecase");
            }
        }
        if (ordens.size() > MAXIMO_DE_CAMPOS) {
            ordens.stream().skip(MAXIMO_DE_CAMPOS).map(Sort.Order::getProperty).forEach(recusados::add);
        }
        if (!recusados.isEmpty()) {
            throw new OrdenacaoInvalidaException(recusados, new TreeSet<>(propriedadePorCampo.keySet()),
                    MAXIMO_DE_CAMPOS);
        }
        List<Sort.Order> traduzidas = ordens.stream()
                .map(ordem -> new Sort.Order(ordem.getDirection(), propriedadePorCampo.get(ordem.getProperty())))
                .toList();
        return Sort.by(traduzidas).and(DESEMPATE);
    }
}
