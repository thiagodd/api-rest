import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gerador de carga mínimo (19.01): N clientes simultâneos repetem a mesma requisição GET durante alguns segundos, e
 * o programa mostra a vazão, a latência (p50, p95, p99, máxima) e os status recebidos. Cada cliente usa a própria
 * conexão, como N usuários diferentes.
 *
 * Uso: java scripts/carga/Carga.java <url> <clientes> <segundos>
 *
 * Serve para observar a saturação de um pool, não para medir desempenho: a ferramenta de verdade (k6) é o assunto
 * da 28.01.
 */
public class Carga {

    public static void main(String[] args) throws Exception {
        var url = URI.create(args[0]);
        int clientes = Integer.parseInt(args[1]);
        var duracao = Duration.ofSeconds(Long.parseLong(args[2]));

        var latencias = Collections.synchronizedList(new ArrayList<Long>());
        var status = new ConcurrentHashMap<String, AtomicInteger>();
        long comeco = System.nanoTime();
        long fim = comeco + duracao.toNanos();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < clientes; i++) {
                executor.submit(() -> {
                    try (var http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                            .connectTimeout(Duration.ofSeconds(10)).build()) {
                        var requisicao = HttpRequest.newBuilder(url).timeout(Duration.ofSeconds(30)).build();
                        while (System.nanoTime() < fim) {
                            long inicio = System.nanoTime();
                            String resultado;
                            try {
                                resultado = String.valueOf(
                                        http.send(requisicao, HttpResponse.BodyHandlers.discarding()).statusCode());
                            }
                            catch (Exception ex) {
                                resultado = ex.getClass().getSimpleName();
                            }
                            latencias.add((System.nanoTime() - inicio) / 1_000_000);
                            status.computeIfAbsent(resultado, s -> new AtomicInteger()).incrementAndGet();
                        }
                    }
                });
            }
        }

        // As requisições que começaram antes do fim terminam depois dele: a vazão usa o tempo real.
        double segundos = (System.nanoTime() - comeco) / 1e9;
        var ordenadas = new ArrayList<>(latencias);
        Collections.sort(ordenadas);
        System.out.printf("%d clientes, %.1f s, %d requisições%n", clientes, segundos, ordenadas.size());
        System.out.printf("vazão: %.1f req/s%n", ordenadas.size() / segundos);
        System.out.printf("latência (ms): p50=%d p95=%d p99=%d máx=%d%n", percentil(ordenadas, 50),
                percentil(ordenadas, 95), percentil(ordenadas, 99), ordenadas.getLast());
        System.out.println("status: " + new TreeMap<>(Map.copyOf(status)));
    }

    private static long percentil(List<Long> ordenadas, int p) {
        int indice = (int) Math.ceil(p / 100.0 * ordenadas.size()) - 1;
        return ordenadas.get(Math.max(indice, 0));
    }
}
