import java.time.Duration;
import java.util.concurrent.Executors;

public class Pinning {
    public static void main(String[] args) throws Exception {
        int tarefas = 120;
        long inicio = System.nanoTime();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < tarefas; i++) {
                var trava = new Object();          // uma trava por tarefa: ninguém disputa
                executor.submit(() -> {
                    synchronized (trava) {
                        Thread.sleep(Duration.ofMillis(100));   // espera dentro do synchronized
                    }
                    return null;
                });
            }
        }
        System.out.printf("Java %s, %d núcleos: %d tarefas de 100 ms em %d ms%n", Runtime.version().feature(),
                Runtime.getRuntime().availableProcessors(), tarefas, (System.nanoTime() - inicio) / 1_000_000);
    }
}
