package com.bazar.experimentos;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simula uma dependência lenta de três formas (19.01): esperando sem fazer nada (I/O bloqueante), segurando uma
 * conexão do banco e ocupando a CPU. Cada resposta diz qual thread a atendeu.
 */
@RestController
@RequestMapping("/experimentos")
@Profile("experimentos")
class LentidaoController {

    private final JdbcClient jdbc;

    LentidaoController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Como uma chamada HTTP a uma API externa lenta: a thread fica parada, esperando a resposta.
    @GetMapping("/espera")
    Map<String, Object> espera(@RequestParam(defaultValue = "200") long ms) throws InterruptedException {
        Thread.sleep(Duration.ofMillis(ms));
        return resposta(ms);
    }

    // Como uma consulta lenta: a thread espera, e uma conexão do pool do banco fica ocupada o tempo todo.
    @GetMapping("/banco")
    Map<String, Object> banco(@RequestParam(defaultValue = "200") long ms) {
        jdbc.sql("select pg_sleep(:segundos)").param("segundos", ms / 1000.0).query().singleValue();
        return resposta(ms);
    }

    // Trabalho de CPU: a thread não espera nada, ela calcula. O laço conta o tempo de CPU da própria thread, e não o
    // do relógio: com mais threads que núcleos, a thread fica parte do tempo fora do processador, e um laço pelo
    // relógio terminaria tendo feito menos trabalho.
    @GetMapping("/cpu")
    Map<String, Object> cpu(@RequestParam(defaultValue = "200") long ms) {
        var cpu = ManagementFactory.getThreadMXBean();
        long fim = cpu.getCurrentThreadCpuTime() + Duration.ofMillis(ms).toNanos();
        long contas = 0;
        while (cpu.getCurrentThreadCpuTime() < fim) {
            contas++;
        }
        var corpo = resposta(ms);
        corpo.put("contas", contas);
        return corpo;
    }

    private static Map<String, Object> resposta(long ms) {
        var corpo = new LinkedHashMap<String, Object>();
        corpo.put("ms", ms);
        corpo.put("thread", Thread.currentThread().toString());
        return corpo;
    }
}
