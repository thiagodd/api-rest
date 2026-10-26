package com.bazar.compartilhado.operacoes;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@TesteDeIntegracao
class FilaDeOperacoesTest {

    static final Duration PRAZO = Duration.ofMinutes(5);

    @Autowired
    FilaDeOperacoes fila;

    @Autowired
    TransactionTemplate transacao;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void duasInstanciasReservandoAoMesmoTempoNaoPegamAMesmaOperacao() throws Exception {
        var primeira = fila.enfileirar("TESTE", "{}");
        var segunda = fila.enfileirar("TESTE", "{}");
        var reservou = new CountDownLatch(1);
        var podeTerminar = new CountDownLatch(1);

        // A "instância A" reserva uma operação e segura a transação aberta, com a linha travada.
        var instanciaA = CompletableFuture.supplyAsync(() -> transacao.execute(status -> {
            var reservadas = fila.reservar(1, PRAZO);
            reservou.countDown();
            aguardar(podeTerminar);
            return reservadas;
        }));
        assertThat(reservou.await(5, TimeUnit.SECONDS)).isTrue();

        // A "instância B" não espera a trava da A: pula a linha travada e pega a outra.
        long inicio = System.nanoTime();
        var daInstanciaB = fila.reservar(5, PRAZO);
        var espera = Duration.ofNanos(System.nanoTime() - inicio);
        podeTerminar.countDown();
        var daInstanciaA = instanciaA.get(5, TimeUnit.SECONDS);

        assertThat(daInstanciaA).extracting(Operacao::id).containsExactly(primeira);
        assertThat(daInstanciaB).extracting(Operacao::id).containsExactly(segunda);
        assertThat(espera).isLessThan(Duration.ofSeconds(1));
    }

    @Test
    void umaOperacaoReservadaSoVoltaDepoisDoPrazo() {
        var id = fila.enfileirar("TESTE", "{}");
        assertThat(fila.reservar(5, PRAZO)).hasSize(1);

        // Dentro do prazo, ninguém mais a pega.
        assertThat(fila.reservar(5, PRAZO)).isEmpty();

        // A instância que a reservou caiu: o prazo venceu, e outra a retoma.
        vencerOPrazo(id);
        assertThat(fila.reservar(5, PRAZO)).singleElement()
                .satisfies(operacao -> assertThat(operacao.tentativas()).isEqualTo(2));
    }

    @Test
    void depoisDeTresTentativasAOperacaoFicaParada() {
        var id = fila.enfileirar("TESTE", "{}");
        for (int tentativa = 1; tentativa <= FilaDeOperacoes.MAXIMO_DE_TENTATIVAS; tentativa++) {
            assertThat(fila.reservar(5, PRAZO)).hasSize(1);
            vencerOPrazo(id);
        }
        assertThat(fila.reservar(5, PRAZO)).isEmpty();
        assertThat(fila.buscar(id)).get().extracting(Operacao::status).isEqualTo(StatusOperacao.EM_EXECUCAO);
    }

    @Test
    void reservarPeloIdNaoPegaOperacaoJaReservada() {
        var id = fila.enfileirar("TESTE", "{}");
        assertThat(fila.reservar(id, PRAZO)).isPresent();
        assertThat(fila.reservar(id, PRAZO)).isEmpty();
    }

    private void vencerOPrazo(UUID id) {
        jdbc.sql("update operacao set reservada_ate = now() - interval '1 minute' where id = ?").param(id).update();
    }

    private static void aguardar(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
