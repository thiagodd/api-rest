package com.bazar.compartilhado.idempotencia;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Apaga as chaves vencidas, de hora em hora. Uma chave vencida já é ignorada na busca; apagar só libera espaço.
 * Rodar em várias instâncias ao mesmo tempo não faz mal: o delete é idempotente.
 */
@Component
class ExpiracaoDasChaves {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoDasChaves.class);

    private final ChavesDeIdempotencia chaves;
    private final Clock relogio;

    ExpiracaoDasChaves(ChavesDeIdempotencia chaves, Clock relogio) {
        this.chaves = chaves;
        this.relogio = relogio;
    }

    @Scheduled(cron = "0 17 * * * *")
    void apagarVencidas() {
        var apagadas = chaves.apagarVencidas(relogio.instant());
        log.info("Chaves de idempotência vencidas apagadas: {}", apagadas);
    }
}
