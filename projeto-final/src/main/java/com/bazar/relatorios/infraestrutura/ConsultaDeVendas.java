package com.bazar.relatorios.infraestrutura;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Relatórios são agregações sobre várias tabelas: SQL direto, sem entidades e sem contexto de persistência (10.05).
 */
@Repository
public class ConsultaDeVendas {

    private final JdbcClient jdbc;

    public ConsultaDeVendas(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Período semiaberto [de, ate): um pedido criado exatamente em "ate" fica para o período seguinte.
    // Pedidos cancelados não contam como venda.
    @Transactional(readOnly = true)
    public List<VendasDoProduto> porProduto(Instant de, Instant ate) {
        return jdbc.sql("""
                select i.produto_id,
                       i.sku,
                       i.nome_produto,
                       count(distinct p.id) as pedidos,
                       sum(i.quantidade)    as unidades,
                       sum(i.subtotal)      as receita
                  from item_pedido i
                  join pedido p on p.id = i.pedido_id
                 where p.criado_em >= :de
                   and p.criado_em <  :ate
                   and p.status <> 'CANCELADO'
                 group by i.produto_id, i.sku, i.nome_produto
                 order by receita desc, i.sku
                """)
                // O driver do PostgreSQL não aceita Instant; para timestamptz, o JDBC 4.2 define OffsetDateTime.
                .param("de", de.atOffset(ZoneOffset.UTC))
                .param("ate", ate.atOffset(ZoneOffset.UTC))
                .query(VendasDoProduto.class)
                .list();
    }
}
