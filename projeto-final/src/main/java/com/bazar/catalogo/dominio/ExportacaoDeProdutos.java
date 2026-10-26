package com.bazar.catalogo.dominio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * O catálogo inteiro, linha a linha (22.02). Nada de List: com um milhão de produtos, uma lista são centenas de
 * megabytes no heap antes de o primeiro byte sair para o cliente.
 */
@Repository
public class ExportacaoDeProdutos {

    // Quantas linhas o driver traz do banco por vez. Sem isso, o driver do PostgreSQL traz o resultado inteiro para a
    // memória na primeira leitura, e o Stream só esconde a lista.
    static final int LINHAS_POR_LOTE = 1000;

    public record LinhaDeProduto(UUID id, String sku, String nome, String status, BigDecimal precoValor,
            String precoMoeda, UUID categoriaId, OffsetDateTime criadoEm) {
    }

    private final JdbcClient jdbc;
    private final TransactionTemplate transacao;

    public ExportacaoDeProdutos(JdbcClient jdbc, PlatformTransactionManager transacoes) {
        this.jdbc = jdbc;
        this.transacao = new TransactionTemplate(transacoes);
        this.transacao.setReadOnly(true);
    }

    /**
     * Entrega cada produto ao destino, em ordem de SKU, sem guardar nenhum. Roda numa transação porque o driver do
     * PostgreSQL só usa um cursor (e respeita o fetch size) com o autocommit desligado.
     */
    public void percorrer(Consumer<LinhaDeProduto> destino) {
        transacao.executeWithoutResult(status -> {
            try (var linhas = jdbc.sql("""
                    select id, sku, nome, status, preco_valor, preco_moeda, categoria_id, criado_em
                      from produto
                     order by sku""")
                    .withFetchSize(LINHAS_POR_LOTE)
                    .query(LinhaDeProduto.class)
                    .stream()) {
                linhas.forEach(destino);
            }
        });
    }
}
