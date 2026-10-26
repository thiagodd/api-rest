package com.bazar.integracoes.pagafacil;

import java.time.Instant;
import java.time.ZoneOffset;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class NotificacoesRecebidas {

    private final JdbcClient jdbc;

    NotificacoesRecebidas(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Grava a notificação, se ela for nova. Devolve false para uma repetição (o mesmo webhook-id). */
    boolean registrar(String origem, String webhookId, String corpo, Instant agora) {
        return jdbc.sql("""
                insert into notificacao_recebida (origem, webhook_id, corpo, recebida_em)
                values (:origem, :id, :corpo, :agora)
                on conflict (origem, webhook_id) do nothing""")
                .param("origem", origem).param("id", webhookId).param("corpo", corpo)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .update() == 1;
    }
}
