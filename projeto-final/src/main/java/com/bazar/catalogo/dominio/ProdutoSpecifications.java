package com.bazar.catalogo.dominio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.PredicateSpecification;

/**
 * Cada filtro da listagem de produtos é uma condição independente; a consulta junta só as que foram pedidas.
 */
public final class ProdutoSpecifications {

    private ProdutoSpecifications() {
    }

    public static PredicateSpecification<Produto> daCategoria(UUID categoriaId) {
        return (produto, cb) -> cb.equal(produto.get("categoriaId"), categoriaId);
    }

    public static PredicateSpecification<Produto> comStatus(StatusProduto status) {
        return (produto, cb) -> cb.equal(produto.get("status"), status);
    }

    public static PredicateSpecification<Produto> custandoAPartirDe(BigDecimal minimo) {
        return (produto, cb) -> cb.greaterThanOrEqualTo(produto.get("preco").get("valor"), minimo);
    }

    public static PredicateSpecification<Produto> custandoAte(BigDecimal maximo) {
        return (produto, cb) -> cb.lessThanOrEqualTo(produto.get("preco").get("valor"), maximo);
    }

    // O texto vira parâmetro do LIKE, nunca parte do SQL. Os curingas digitados pelo cliente são escapados.
    public static PredicateSpecification<Produto> comNomeContendo(String texto) {
        var padrao = "%" + texto.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return (produto, cb) -> cb.like(cb.lower(produto.get("nome")), padrao, '\\');
    }

    public static PredicateSpecification<Produto> filtradoPor(FiltroDeProdutos filtro) {
        var condicoes = new ArrayList<PredicateSpecification<Produto>>();
        if (filtro.categoriaId() != null) {
            condicoes.add(daCategoria(filtro.categoriaId()));
        }
        if (filtro.status() != null) {
            condicoes.add(comStatus(filtro.status()));
        }
        if (filtro.precoMinimo() != null) {
            condicoes.add(custandoAPartirDe(filtro.precoMinimo()));
        }
        if (filtro.precoMaximo() != null) {
            condicoes.add(custandoAte(filtro.precoMaximo()));
        }
        if (filtro.texto() != null) {
            condicoes.add(comNomeContendo(filtro.texto()));
        }
        // Sem condições, allOf não restringe nada: todos os produtos.
        return PredicateSpecification.allOf(condicoes);
    }
}
