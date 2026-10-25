package com.bazar.compartilhado.web;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.bazar.compartilhado.erros.ValorNaoAceitoException;
import org.jspecify.annotations.Nullable;

/**
 * Lê um parâmetro com uma lista de nomes separados por vírgula, como fields=id,nome ou embed=categoria (13.05).
 * Um nome fora da lista é recusado (400), e não ignorado: o cliente receberia outra coisa sem saber.
 */
public final class ValoresDeParametro {

    private ValoresDeParametro() {
    }

    /**
     * @return os nomes pedidos, na ordem em que vieram; vazio se o parâmetro não veio
     */
    public static Set<String> ler(String parametro, @Nullable String valor, SortedSet<String> aceitos) {
        if (valor == null || valor.isBlank()) {
            return Set.of();
        }
        var pedidos = new LinkedHashSet<String>();
        Arrays.stream(valor.split(",", -1)).map(String::strip).forEach(pedidos::add);
        var recusados = new TreeSet<>(pedidos);
        recusados.removeAll(aceitos);
        if (!recusados.isEmpty()) {
            throw new ValorNaoAceitoException(parametro, recusados, aceitos);
        }
        return pedidos;
    }
}
