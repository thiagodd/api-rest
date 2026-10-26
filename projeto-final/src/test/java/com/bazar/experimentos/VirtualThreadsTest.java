package com.bazar.experimentos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bazar.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O mesmo experimento da 19.01 com virtual threads (19.02): o threads.max=4 não vale mais, e cada requisição ganha
 * uma virtual thread. Vinte esperas de 200 ms acontecem ao mesmo tempo, em vez de cinco rodadas.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "server.tomcat.threads.max=4")
@ActiveProfiles("experimentos")
@Import(TestcontainersConfiguration.class)
class VirtualThreadsTest {

    @LocalServerPort
    int porta;

    @Test
    void vinteRequisicoesAcontecemAoMesmoTempo() throws Exception {
        long inicio = System.nanoTime();
        var respostas = new ArrayList<Future<String>>();
        try (var clientes = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 20; i++) {
                respostas.add(clientes.submit(() -> get("/experimentos/espera?ms=200")));
            }
        }
        var total = Duration.ofNanos(System.nanoTime() - inicio);

        // Com 4 threads de plataforma, seriam pelo menos 1.000 ms (SaturacaoDoPoolTest). A folga cobre máquinas lentas.
        assertThat(total).isLessThan(Duration.ofMillis(900));
        for (var resposta : respostas) {
            // VirtualThread[#123,tomcat-handler-7]/runnable@ForkJoinPool-1-worker-3
            assertThat(resposta.get()).contains("VirtualThread").contains("tomcat-handler-");
        }
    }

    private String get(String caminho) throws Exception {
        try (var http = HttpClient.newHttpClient()) {
            var requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).build();
            var resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());
            assertThat(resposta.statusCode()).isEqualTo(200);
            return resposta.body();
        }
    }
}
