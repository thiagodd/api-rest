package com.bazar.reativo;

import java.time.Duration;
import java.util.Map;

import reactor.core.publisher.Mono;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Os experimentos da 19.01 e da 19.02, em versão reativa (19.05).
 */
@RestController
@RequestMapping("/experimentos")
class ExperimentosController {

    private final DatabaseClient banco;

    ExperimentosController(DatabaseClient banco) {
        this.banco = banco;
    }

    // Como uma consulta lenta: uma conexão do pool do R2DBC fica ocupada, mas nenhuma thread espera.
    @GetMapping("/banco")
    Mono<Map<String, Object>> banco(@RequestParam(defaultValue = "200") long ms) {
        return banco.sql("select pg_sleep(:segundos)").bind("segundos", ms / 1000.0)
                .fetch().rowsUpdated()
                .map(linhas -> resposta(ms));
    }

    // Espera sem bloquear: o Reactor agenda a continuação para daqui a ms milissegundos e libera a thread.
    @GetMapping("/espera")
    Mono<Map<String, Object>> espera(@RequestParam(defaultValue = "200") long ms) {
        return Mono.delay(Duration.ofMillis(ms)).map(tique -> resposta(ms));
    }

    // O erro clássico: uma chamada bloqueante dentro do event loop. A thread do Netty fica parada, e todas as outras
    // conexões que ela atende esperam junto.
    @GetMapping("/bloqueio")
    Mono<Map<String, Object>> bloqueio(@RequestParam(defaultValue = "200") long ms) throws InterruptedException {
        Thread.sleep(ms);
        return Mono.just(resposta(ms));
    }

    private static Map<String, Object> resposta(long ms) {
        return Map.of("ms", ms, "thread", Thread.currentThread().toString());
    }
}
