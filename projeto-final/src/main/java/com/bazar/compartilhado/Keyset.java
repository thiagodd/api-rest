package com.bazar.compartilhado;

import java.util.ArrayList;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.PredicateSpecification;

/**
 * A condição da paginação por cursor para uma ordenação qualquer (13.04): os itens que vêm depois da posição (ou
 * antes, para trás). Com sort=preco,desc e o desempate pelo id, e a posição (p, i), fica
 *
 * <pre>preco &lt;= p and (preco &lt; p or (preco = p and id &gt; i))</pre>
 *
 * Com direções misturadas, não dá para comparar uma linha, como (preco, id) &lt; (p, i). O primeiro termo é
 * redundante, mas é o que um índice que começa pelo primeiro campo usa como limite: sem ele, o banco percorre o
 * índice desde o início e descarta cada linha antes da posição.
 */
public final class Keyset {

    private Keyset() {
    }

    /**
     * @param ordenacao a ordenação da listagem, com um desempate único no final
     * @param posicao as chaves da ordenação de um item já visto, com os nomes das propriedades
     */
    @SuppressWarnings("unchecked")
    public static <T> PredicateSpecification<T> alemDe(Sort ordenacao, KeysetScrollPosition posicao) {
        return (raiz, cb) -> {
            var chaves = posicao.getKeys();
            var alternativas = new ArrayList<Predicate>();
            var iguaisAte = new ArrayList<Predicate>();
            Predicate limite = null;
            for (var ordem : ordenacao) {
                var campo = caminho(raiz, ordem.getProperty());
                var valor = (Comparable<Object>) chaves.get(ordem.getProperty());
                var crescente = ordem.isAscending() != posicao.scrollsBackward();
                var adiante = new ArrayList<>(iguaisAte);
                adiante.add(crescente ? cb.greaterThan(campo, valor) : cb.lessThan(campo, valor));
                alternativas.add(cb.and(adiante.toArray(Predicate[]::new)));
                iguaisAte.add(cb.equal(campo, valor));
                if (limite == null) {
                    limite = crescente ? cb.greaterThanOrEqualTo(campo, valor) : cb.lessThanOrEqualTo(campo, valor);
                }
            }
            return cb.and(limite, cb.or(alternativas.toArray(Predicate[]::new)));
        };
    }

    // "preco.valor" é o campo valor do embeddable preco.
    @SuppressWarnings("unchecked")
    private static Expression<Comparable<Object>> caminho(From<?, ?> raiz, String propriedade) {
        Path<?> caminho = raiz;
        for (var parte : propriedade.split("\\.")) {
            caminho = caminho.get(parte);
        }
        return (Expression<Comparable<Object>>) caminho;
    }
}
