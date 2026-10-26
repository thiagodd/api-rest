package com.bazar.integracoes.webhooks;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bazar.compartilhado.Ids;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class AssinaturasWebhook {

    private final JdbcClient jdbc;

    AssinaturasWebhook(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    AssinaturaWebhook criar(String parceiro, URI url, List<String> eventos, String segredo, Instant agora) {
        var id = Ids.novo();
        jdbc.sql("""
                insert into assinatura_webhook (id, parceiro, url, eventos, segredo, ativa, criada_em)
                values (:id, :parceiro, :url, :eventos, :segredo, true, :agora)""")
                .param("id", id).param("parceiro", parceiro).param("url", url.toString())
                .param("eventos", String.join(",", eventos)).param("segredo", segredo)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .update();
        return new AssinaturaWebhook(id, parceiro, url, eventos, segredo, true, agora, null);
    }

    Optional<AssinaturaWebhook> buscar(UUID id) {
        return jdbc.sql("""
                select id, parceiro, url, eventos, segredo, ativa, criada_em, desativada_em
                  from assinatura_webhook where id = :id""")
                .param("id", id)
                .query((linha, n) -> new AssinaturaWebhook(linha.getObject("id", UUID.class),
                        linha.getString("parceiro"), URI.create(linha.getString("url")),
                        Arrays.asList(linha.getString("eventos").split(",")), linha.getString("segredo"),
                        linha.getBoolean("ativa"), linha.getObject("criada_em", OffsetDateTime.class).toInstant(),
                        Optional.ofNullable(linha.getObject("desativada_em", OffsetDateTime.class))
                                .map(OffsetDateTime::toInstant).orElse(null)))
                .optional();
    }

    void desativar(UUID id, Instant agora) {
        jdbc.sql("update assinatura_webhook set ativa = false, desativada_em = :agora where id = :id and ativa")
                .param("id", id).param("agora", agora.atOffset(ZoneOffset.UTC))
                .update();
    }
}
