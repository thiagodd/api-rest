package com.bazar.experimentos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
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
 * O pool de threads do Tomcat como gargalo (19.01). Com 4 threads e um endpoint de 200 ms, 20 requisições simultâneas
 * precisam de 5 rodadas: pelo menos 1 s no total, por mais rápida que seja a máquina. Os testes só conferem limites
 * inferiores, que não dependem da máquina.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = "server.tomcat.threads.max=4")
@ActiveProfiles("experimentos")
@Import(TestcontainersConfiguration.class)
class SaturacaoDoPoolTest {

    @LocalServerPort
    int porta;

    @Test
    void vinteRequisicoesComQuatroThreadsLevamCincoRodadas() throws Exception {
        long inicio = System.nanoTime();
        var threads = new HashSet<String>();
        try (var clientes = Executors.newVirtualThreadPerTaskExecutor()) {
            var respostas = new ArrayList<Future<String>>();
            for (int i = 0; i < 20; i++) {
                respostas.add(clientes.submit(() -> get("/experimentos/espera?ms=200")));
            }
            for (var resposta : respostas) {
                threads.add(resposta.get().replaceAll(".*(http-nio-\\d+-exec-\\d+).*", "$1"));
            }
        }
        var total = Duration.ofNanos(System.nanoTime() - inicio);

        // Lei de Little: vazão máxima = 4 threads / 0,2 s = 20 req/s. Vinte requisições levam pelo menos 1 s.
        assertThat(total).isGreaterThanOrEqualTo(Duration.ofMillis(1000));
        assertThat(threads).hasSizeLessThanOrEqualTo(4);
    }

    @Test
    void umaRotaRapidaEsperaNaFilaAtrasDasLentas() throws Exception {
        try (var clientes = Executors.newVirtualThreadPerTaskExecutor()) {
            // Quatro requisições lentas ocupam as quatro threads por 500 ms.
            for (int i = 0; i < 4; i++) {
                clientes.submit(() -> get("/experimentos/espera?ms=500"));
            }
            Thread.sleep(100);

            long inicio = System.nanoTime();
            get("/experimentos/espera?ms=0");
            var espera = Duration.ofNanos(System.nanoTime() - inicio);

            // A rota não faz nada, mas só começa quando uma thread se libera: uns 400 ms depois.
            assertThat(espera).isGreaterThanOrEqualTo(Duration.ofMillis(300));
        }
    }

    private String get(String caminho) throws Exception {
        // Um HttpClient por chamada: uma conexão por cliente, como usuários diferentes.
        try (var http = HttpClient.newHttpClient()) {
            var requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).build();
            var resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());
            assertThat(resposta.statusCode()).isEqualTo(200);
            return resposta.body();
        }
    }
}
