package com.bazar;

import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * Apaga o que os testes gravaram, preservando os dados de referência das migrations (as categorias iniciais).
 * A ordem respeita as chaves estrangeiras.
 */
public final class LimpezaDoBanco {

    private LimpezaDoBanco() {
    }

    public static void limpar(JdbcClient jdbc) {
        jdbc.sql("delete from chave_idempotencia").update();
        jdbc.sql("delete from entrega_webhook").update();
        jdbc.sql("delete from assinatura_webhook").update();
        jdbc.sql("delete from notificacao_recebida").update();
        jdbc.sql("delete from operacao").update();
        jdbc.sql("delete from historico_pedido").update();
        jdbc.sql("delete from item_pedido").update();
        jdbc.sql("delete from pedido").update();
        jdbc.sql("delete from estoque").update();
        jdbc.sql("delete from produto").update();
        jdbc.sql("""
                delete from categoria
                 where id not in ('0192f3a0-5555-7000-8000-000000000001', '0192f3a0-5555-7000-8000-000000000002',
                                  '0192f3a0-5555-7000-8000-000000000003')
                   and pai_id is not null""").update();
        jdbc.sql("""
                delete from categoria
                 where id not in ('0192f3a0-5555-7000-8000-000000000001', '0192f3a0-5555-7000-8000-000000000002',
                                  '0192f3a0-5555-7000-8000-000000000003')""").update();
    }
}
