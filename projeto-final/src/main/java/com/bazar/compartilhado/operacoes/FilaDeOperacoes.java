package com.bazar.compartilhado.operacoes;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bazar.compartilhado.Ids;
import org.jspecify.annotations.Nullable;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * A tabela operacao usada como fila (19.03). Várias instâncias do Bazar podem reservar operações ao mesmo tempo: o
 * FOR UPDATE SKIP LOCKED faz cada uma pular as linhas que outra já travou, em vez de esperar por elas.
 */
@Repository
public class FilaDeOperacoes {

    // Depois de três tentativas (três instâncias que caíram no meio), a operação fica parada para alguém olhar.
    static final int MAXIMO_DE_TENTATIVAS = 3;

    private final JdbcClient jdbc;
    private final Clock relogio;

    public FilaDeOperacoes(JdbcClient jdbc, Clock relogio) {
        this.jdbc = jdbc;
        this.relogio = relogio;
    }

    public UUID enfileirar(String tipo, String parametros) {
        var id = Ids.novo();
        var agora = relogio.instant().atOffset(ZoneOffset.UTC);
        jdbc.sql("""
                insert into operacao (id, tipo, status, parametros, criada_em, atualizada_em)
                values (:id, :tipo, 'PENDENTE', :parametros, :agora, :agora)
                """)
                .param("id", id).param("tipo", tipo).param("parametros", parametros).param("agora", agora)
                .update();
        return id;
    }

    /**
     * Reserva até {@code limite} operações: as pendentes e as que ficaram em execução numa instância que não terminou
     * no prazo (caiu, travou). Numa transação só, o UPDATE marca e devolve as linhas que o SELECT travou.
     */
    public List<Operacao> reservar(int limite, Duration prazo) {
        return reservar("true", limite, prazo, null);
    }

    /** Reserva uma operação específica, se ela ainda estiver disponível (ninguém a pegou antes). */
    public Optional<Operacao> reservar(UUID id, Duration prazo) {
        return reservar("id = :id", 1, prazo, id).stream().findFirst();
    }

    private List<Operacao> reservar(String filtro, int limite, Duration prazo, @Nullable UUID id) {
        var agora = relogio.instant().atOffset(ZoneOffset.UTC);
        var consulta = jdbc.sql("""
                update operacao
                   set status = 'EM_EXECUCAO', tentativas = tentativas + 1,
                       reservada_ate = :reservadaAte, atualizada_em = :agora
                 where id in (select id
                                from operacao
                               where %s
                                 and (status = 'PENDENTE'
                                      or (status = 'EM_EXECUCAO' and reservada_ate < :agora))
                                 and tentativas < :maximo
                               order by criada_em
                               limit :limite
                                 for update skip locked)
                returning id, tipo, status, parametros, tentativas, resultado, erro, criada_em, atualizada_em
                """.formatted(filtro))
                .param("agora", agora)
                .param("reservadaAte", agora.plus(prazo))
                .param("maximo", MAXIMO_DE_TENTATIVAS)
                .param("limite", limite);
        if (id != null) {
            consulta = consulta.param("id", id);
        }
        return consulta.query(Operacao.class).list();
    }

    public void concluir(UUID id, String resultado) {
        finalizar(id, StatusOperacao.CONCLUIDA, resultado, null);
    }

    public void falhar(UUID id, String erro) {
        finalizar(id, StatusOperacao.FALHOU, null, erro);
    }

    public Optional<Operacao> buscar(UUID id) {
        return jdbc.sql("""
                select id, tipo, status, parametros, tentativas, resultado, erro, criada_em, atualizada_em
                  from operacao where id = :id
                """).param("id", id).query(Operacao.class).optional();
    }

    private void finalizar(UUID id, StatusOperacao status, @Nullable String resultado,
            @Nullable String erro) {
        jdbc.sql("""
                update operacao
                   set status = :status, resultado = :resultado, erro = :erro, reservada_ate = null,
                       atualizada_em = :agora
                 where id = :id
                """)
                .param("id", id).param("status", status.name()).param("resultado", resultado)
                .param("erro", erro).param("agora", relogio.instant().atOffset(ZoneOffset.UTC))
                .update();
    }
}
