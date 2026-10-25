package com.bazar.catalogo.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import org.springframework.data.jpa.domain.PredicateSpecification;

/**
 * Cada filtro da listagem de produtos é uma condição independente; a consulta junta só as que foram pedidas.
 */
public final class ProdutoSpecifications {

    private ProdutoSpecifications() {
    }

    public static PredicateSpecification<Produto> daCategoriaEm(Collection<UUID> categoriaIds) {
        return (produto, cb) -> produto.get("categoriaId").in(categoriaIds);
    }

    public static PredicateSpecification<Produto> comStatusEm(Collection<StatusProduto> status) {
        return (produto, cb) -> produto.get("status").in(status);
    }

    public static PredicateSpecification<Produto> comSku(String sku) {
        return (produto, cb) -> cb.equal(produto.get("sku"), sku);
    }

    public static PredicateSpecification<Produto> criadoApos(Instant instante) {
        return (produto, cb) -> cb.greaterThan(produto.get("criadoEm"), instante);
    }

    // atributos é uma coluna jsonb: jsonb_extract_path_text(atributos, 'autor') devolve o valor como texto.
    // A chave e o valor vão como parâmetros da consulta (?). cb.literal(chave) escreveria a chave dentro do SQL;
    // o value do Hibernate a transforma em parâmetro, como o valor do equal e o padrão do LIKE abaixo.
    public static PredicateSpecification<Produto> comAtributo(String chave, String valor) {
        return (produto, cb) -> cb.equal(
                cb.function("jsonb_extract_path_text", String.class, produto.get("atributos"),
                        ((HibernateCriteriaBuilder) cb).value(chave)),
                valor);
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
        if (filtro.texto() != null) {
            condicoes.add(comNomeContendo(filtro.texto()));
        }
        if (filtro.sku() != null) {
            condicoes.add(comSku(filtro.sku()));
        }
        if (!filtro.categoriaIds().isEmpty()) {
            condicoes.add(daCategoriaEm(filtro.categoriaIds()));
        }
        if (!filtro.status().isEmpty()) {
            condicoes.add(comStatusEm(filtro.status()));
        }
        if (filtro.precoMinimo() != null) {
            condicoes.add(custandoAPartirDe(filtro.precoMinimo()));
        }
        if (filtro.precoMaximo() != null) {
            condicoes.add(custandoAte(filtro.precoMaximo()));
        }
        if (filtro.criadoApos() != null) {
            condicoes.add(criadoApos(filtro.criadoApos()));
        }
        filtro.atributos().forEach((chave, valor) -> condicoes.add(comAtributo(chave, valor)));
        // Sem condições, allOf não restringe nada: todos os produtos.
        return PredicateSpecification.allOf(condicoes);
    }
}
