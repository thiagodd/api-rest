package com.bazar.compartilhado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

/**
 * Monta o Window de uma paginação por cursor (13.04) a partir de uma busca que pediu um item a mais que o limite:
 * se ele veio, há outra página na mesma direção, e nenhum count foi preciso.
 */
public final class Janelas {

    private Janelas() {
    }

    /**
     * @param encontrados até limite + 1 itens; para trás, na ordem inversa da listagem (do mais próximo da posição
     *        para o mais distante)
     * @param chaves as chaves da ordenação de um item, para a posição de cada um no Window
     * @return os itens na ordem da listagem; hasNext diz se há mais na direção pedida
     */
    public static <T> Window<T> de(List<T> encontrados, int limite, boolean paraTras,
            Function<T, Map<String, ?>> chaves) {
        var itens = new ArrayList<>(encontrados.subList(0, Math.min(limite, encontrados.size())));
        if (paraTras) {
            Collections.reverse(itens);
        }
        return Window.from(itens, i -> ScrollPosition.forward(chaves.apply(itens.get(i))),
                encontrados.size() > limite);
    }
}
