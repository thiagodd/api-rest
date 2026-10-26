package com.bazar.integracoes.webhooks;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * O outbox dos webhooks (21.03): uma linha por evento e assinatura, gravada na transação que produziu o evento.
 */
@Repository
class EntregasDeWebhook {

    private final JdbcClient jdbc;

    EntregasDeWebhook(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Uma entrega para cada assinatura ativa que quer este tipo de evento. Devolve quantas. */
    int enfileirar(String tipoEvento, UUID eventoId, String corpo, Instant agora) {
        return jdbc.sql("""
                insert into entrega_webhook (id, assinatura_id, evento_id, tipo_evento, corpo, status,
                                             proxima_tentativa_em, criada_em)
                select gen_random_uuid(), a.id, :evento, :tipo, :corpo, 'PENDENTE', :agora, :agora
                  from assinatura_webhook a
                 where a.ativa and :tipo = any (string_to_array(a.eventos, ','))""")
                .param("evento", eventoId).param("tipo", tipoEvento).param("corpo", corpo)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .update();
    }

    record Reservada(UUID id, UUID assinaturaId, UUID eventoId, String corpo, int tentativas, URI url,
            String segredo) {
    }

    /**
     * Reserva até {@code limite} entregas vencidas, como a fila de operações da 19.03: empurra a próxima tentativa
     * para depois do prazo, e outra instância não as pega enquanto esta envia. Se esta cair no meio, o prazo vence
     * e a entrega volta a ficar disponível.
     */
    List<Reservada> reservar(int limite, Duration prazo, Instant agora) {
        return jdbc.sql("""
                with reservadas as (
                    update entrega_webhook
                       set tentativas = tentativas + 1, proxima_tentativa_em = :prazo
                     where id in (select e.id
                                    from entrega_webhook e
                                    join assinatura_webhook a on a.id = e.assinatura_id
                                   where e.status = 'PENDENTE' and e.proxima_tentativa_em <= :agora and a.ativa
                                   order by e.proxima_tentativa_em
                                   limit :limite
                                     for update of e skip locked)
                    returning id, assinatura_id, evento_id, corpo, tentativas)
                select r.id, r.assinatura_id, r.evento_id, r.corpo, r.tentativas, a.url, a.segredo
                  from reservadas r join assinatura_webhook a on a.id = r.assinatura_id""")
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .param("prazo", agora.plus(prazo).atOffset(ZoneOffset.UTC))
                .param("limite", limite)
                .query((linha, n) -> new Reservada(linha.getObject("id", UUID.class),
                        linha.getObject("assinatura_id", UUID.class), linha.getObject("evento_id", UUID.class),
                        linha.getString("corpo"), linha.getInt("tentativas"), URI.create(linha.getString("url")),
                        linha.getString("segredo")))
                .list();
    }

    void marcarEntregue(UUID id, int statusHttp, Instant agora) {
        jdbc.sql("""
                update entrega_webhook
                   set status = 'ENTREGUE', ultimo_status_http = :http, ultimo_erro = null, entregue_em = :agora
                 where id = :id""")
                .param("id", id).param("http", statusHttp).param("agora", agora.atOffset(ZoneOffset.UTC))
                .update();
    }

    void agendarNovaTentativa(UUID id, @Nullable Integer statusHttp, @Nullable String erro, Instant quando) {
        jdbc.sql("""
                update entrega_webhook
                   set ultimo_status_http = :http, ultimo_erro = :erro, proxima_tentativa_em = :quando
                 where id = :id""")
                .param("id", id).param("http", statusHttp).param("erro", erro)
                .param("quando", quando.atOffset(ZoneOffset.UTC))
                .update();
    }

    void marcarFalhou(UUID id, @Nullable Integer statusHttp, @Nullable String erro) {
        jdbc.sql("""
                update entrega_webhook
                   set status = 'FALHOU', ultimo_status_http = :http, ultimo_erro = :erro
                 where id = :id""")
                .param("id", id).param("http", statusHttp).param("erro", erro)
                .update();
    }

    List<EntregaWebhook> daAssinatura(UUID assinaturaId, int limite) {
        return jdbc.sql("""
                select id, evento_id, tipo_evento, status, tentativas, ultimo_status_http, ultimo_erro,
                       case when status = 'PENDENTE' then proxima_tentativa_em end as proxima_tentativa_em,
                       criada_em, entregue_em
                  from entrega_webhook
                 where assinatura_id = :assinatura
                 order by criada_em desc
                 limit :limite""")
                .param("assinatura", assinaturaId).param("limite", limite)
                .query(EntregaWebhook.class)
                .list();
    }
}
