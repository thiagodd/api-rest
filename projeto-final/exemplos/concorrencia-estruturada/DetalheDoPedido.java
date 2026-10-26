import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

/**
 * O detalhe do pedido com frete e pagamento consultados em paralelo (19.06): StructuredTaskScope (preview no JDK 25,
 * JEP 505) e ScopedValue (final no JDK 25, JEP 506), comparados com CompletableFuture.
 *
 * Uso: java --enable-preview --source 25 DetalheDoPedido.java <sucesso|falha|timeout|completable|observar>
 *
 * Com "observar", as consultas demoram 30 s: dá tempo de ver a árvore de tarefas num thread dump em JSON,
 * com jcmd <pid> Thread.dump_to_file -format=json <arquivo>.
 *
 * As duas "APIs externas" são simuladas com Thread.sleep: o que interessa aqui é quem espera, quem é cancelado e
 * quando. Um RestClient sobre o HttpClient do JDK também responde à interrupção, cancelando a requisição.
 */
public class DetalheDoPedido {

    // O id de correlação da requisição, visível em todas as subtarefas sem ser passado como parâmetro.
    static final ScopedValue<String> CORRELACAO = ScopedValue.newInstance();

    static final long INICIO = System.nanoTime();

    record Frete(String transportadora, String valor) {
    }

    record Pagamento(String status) {
    }

    record Detalhe(String pedido, Frete frete, Pagamento pagamento) {
    }

    public static void main(String[] args) throws Exception {
        var cenario = args.length > 0 ? args[0] : "sucesso";
        // ScopedValue.where(...).call(...): o valor existe só dentro desta chamada, e some sozinho no fim.
        ScopedValue.where(CORRELACAO, "req-7f3a").call(() -> {
            try {
                var detalhe = switch (cenario) {
                    case "sucesso" -> detalhe(300, 500, false, Duration.ofSeconds(2));
                    case "falha" -> detalhe(2_000, 100, true, Duration.ofSeconds(5));
                    case "timeout" -> detalhe(2_000, 500, false, Duration.ofMillis(800));
                    case "completable" -> comCompletableFuture(2_000, 100, true);
                    case "observar" -> detalhe(30_000, 30_000, false, Duration.ofMinutes(1));
                    default -> throw new IllegalArgumentException(cenario);
                };
                log("detalhe pronto: " + detalhe);
            }
            catch (Exception ex) {
                log("detalhe falhou: " + ex);
            }
            return null;
        });
        if (cenario.equals("completable")) {
            // Dá tempo de ver o que o CompletableFuture deixou rodando depois da falha.
            Thread.sleep(2_200);
        }
        log("fim do programa");
    }

    // A versão estruturada: as duas consultas são filhas deste bloco. Se uma falha, ou o prazo vence, a outra é
    // cancelada, e o bloco só termina quando as duas terminaram.
    static Detalhe detalhe(long msFrete, long msPagamento, boolean pagamentoFalha, Duration prazo)
            throws InterruptedException {
        try (var escopo = StructuredTaskScope.open(Joiner.<Object>awaitAllSuccessfulOrThrow(),
                config -> config.withTimeout(prazo))) {
            var frete = escopo.fork(() -> consultarFrete(msFrete));
            var pagamento = escopo.fork(() -> consultarPagamento(msPagamento, pagamentoFalha));
            escopo.join();
            return new Detalhe("BZR-2026-000004", frete.get(), pagamento.get());
        }
    }

    // A versão com CompletableFuture: as duas consultas também rodam em paralelo, mas não têm dono. A falha do
    // pagamento faz o join() lançar, e o frete continua rodando até o fim, sem ninguém para usar o resultado.
    static Detalhe comCompletableFuture(long msFrete, long msPagamento, boolean pagamentoFalha) {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var frete = CompletableFuture.supplyAsync(() -> consultarFrete(msFrete), executor);
        var pagamento = CompletableFuture.supplyAsync(() -> consultarPagamento(msPagamento, pagamentoFalha), executor);
        return frete.thenCombine(pagamento, (f, p) -> new Detalhe("BZR-2026-000004", f, p)).join();
    }

    static Frete consultarFrete(long ms) {
        log("frete: consultando a transportadora");
        dormir("frete", ms);
        log("frete: respondeu");
        return new Frete("Rápido Entregas", "18.90");
    }

    static Pagamento consultarPagamento(long ms, boolean falha) {
        log("pagamento: consultando a PagaFácil");
        dormir("pagamento", ms);
        if (falha) {
            log("pagamento: a PagaFácil respondeu 503");
            throw new IllegalStateException("PagaFácil indisponível (503)");
        }
        log("pagamento: respondeu");
        return new Pagamento("APROVADO");
    }

    static void dormir(String quem, long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException ex) {
            log(quem + ": cancelado (interrompido)");
            Thread.currentThread().interrupt();
            throw new IllegalStateException(quem + " cancelado");
        }
    }

    static void log(String mensagem) {
        long ms = Duration.ofNanos(System.nanoTime() - INICIO).toMillis();
        var correlacao = CORRELACAO.isBound() ? CORRELACAO.get() : "(sem correlação)";
        System.out.printf("%5d ms  %-16s %-12s %s%n", ms, correlacao, nomeDaThread(), mensagem);
    }

    private static String nomeDaThread() {
        var thread = Thread.currentThread();
        return (thread.isVirtual() ? "virtual#" : "") + (thread.getName().isEmpty() ? thread.threadId() : thread.getName());
    }
}
