package com.bazar.compartilhado.idempotencia;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * As chaves de idempotência no Postgres, no mesmo banco (e na mesma transação) da operação que elas protegem.
 */
@Repository
class ChavesDeIdempotencia {

    private final JdbcClient jdbc;

    ChavesDeIdempotencia(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Um advisory lock da transação, sem esperar: se outra requisição com a mesma chave está em andamento, ela tem
    // o lock, e esta desiste na hora (409). O lock é solto sozinho no commit ou no rollback.
    boolean travar(String escopo, String chave) {
        return jdbc.sql("select pg_try_advisory_xact_lock(hashtextextended(:escopo || ' ' || :chave, 0))")
                .param("escopo", escopo)
                .param("chave", chave)
                .query(Boolean.class)
                .single();
    }

    Optional<RespostaGuardada> buscar(String escopo, String chave, Instant agora) {
        return jdbc.sql("""
                select hash_requisicao, status_resposta, headers_resposta, corpo_resposta
                  from chave_idempotencia
                 where escopo = :escopo and chave = :chave and expira_em > :agora""")
                .param("escopo", escopo)
                .param("chave", chave)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .query((linha, n) -> new RespostaGuardada(linha.getString("hash_requisicao"),
                        linha.getInt("status_resposta"), lerHeaders(linha.getString("headers_resposta")),
                        linha.getBytes("corpo_resposta")))
                .optional();
    }

    // O "on conflict" só acontece com uma chave vencida que a limpeza ainda não apagou: a nova resposta a substitui.
    void guardar(String escopo, String chave, RespostaGuardada resposta, Instant agora, Instant expiraEm) {
        jdbc.sql("""
                insert into chave_idempotencia (escopo, chave, hash_requisicao, status_resposta, headers_resposta,
                                                corpo_resposta, criada_em, expira_em)
                values (:escopo, :chave, :hash, :status, :headers, :corpo, :agora, :expiraEm)
                on conflict (escopo, chave) do update
                   set hash_requisicao = excluded.hash_requisicao, status_resposta = excluded.status_resposta,
                       headers_resposta = excluded.headers_resposta, corpo_resposta = excluded.corpo_resposta,
                       criada_em = excluded.criada_em, expira_em = excluded.expira_em""")
                .param("escopo", escopo)
                .param("chave", chave)
                .param("hash", resposta.hashRequisicao())
                .param("status", resposta.status())
                .param("headers", escreverHeaders(resposta.headers()))
                .param("corpo", resposta.corpo())
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .param("expiraEm", expiraEm.atOffset(ZoneOffset.UTC))
                .update();
    }

    int apagarVencidas(Instant agora) {
        return jdbc.sql("delete from chave_idempotencia where expira_em <= :agora")
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .update();
    }

    // Uma linha por header, "Nome: valor", como no próprio HTTP.
    private static String escreverHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
                .map(header -> header.getKey() + ": " + header.getValue())
                .collect(Collectors.joining("\n"));
    }

    private static Map<String, String> lerHeaders(String texto) {
        var headers = new LinkedHashMap<String, String>();
        texto.lines().forEach(linha -> {
            var separador = linha.indexOf(": ");
            headers.put(linha.substring(0, separador), linha.substring(separador + 2));
        });
        return headers;
    }
}
