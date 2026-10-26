package com.bazar.compartilhado.limites;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.optimization.Optimizations;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Rate limiting por cliente (23.01). Cada requisição consome uma ficha de cada política que vale para ela; sem ficha
 * numa delas, 429 com Retry-After. Toda resposta diz as políticas e o que resta, nos headers RateLimit-Policy e
 * RateLimit (draft-ietf-httpapi-ratelimit-headers-11).
 * <p>
 * O cliente, por enquanto, é o IP: o Bazar ainda não autentica ninguém. Com o JWT (26.04), a chave passa a ser o
 * cliente do token, e o IP fica só para quem não se autentica.
 */
public class LimiteDeRequisicoesFilter extends OncePerRequestFilter {

    public static final String RATE_LIMIT = "RateLimit";
    public static final String RATE_LIMIT_POLICY = "RateLimit-Policy";

    private static final int PROXIES_EM_MEMORIA = 10_000;

    private static final Logger log = LoggerFactory.getLogger(LimiteDeRequisicoesFilter.class);

    record Politica(String nome, long capacidade, long janelaEmSegundos, BucketConfiguration configuracao) {

        static Politica de(String nome, PoliticasDeLimite.Politica politica) {
            // Token bucket com reabastecimento contínuo (greedy): 100 por minuto é uma ficha a cada 0,6 s, e não 100
            // fichas de uma vez a cada minuto. Rajadas de até "capacidade", e nunca mais que isso por janela.
            var banda = Bandwidth.builder()
                    .capacity(politica.capacidade())
                    .refillGreedy(politica.capacidade(), politica.janela())
                    .build();
            return new Politica(nome, politica.capacidade(), politica.janela().toSeconds(),
                    BucketConfiguration.builder().addLimit(banda).build());
        }

        String descricao() {
            return "\"%s\";q=%d;w=%d".formatted(nome, capacidade, janelaEmSegundos);
        }
    }

    private final ObjectProvider<ProxyManager<byte[]>> baldes;
    private final Politica geral;
    private final Politica exportacao;
    private final HandlerExceptionResolver erros;
    private final Map<String, BucketProxy> proxies = Collections.synchronizedMap(
            new LinkedHashMap<>(1024, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BucketProxy> maisAntigo) {
                    return size() > PROXIES_EM_MEMORIA;
                }
            });

    public LimiteDeRequisicoesFilter(ObjectProvider<ProxyManager<byte[]>> baldes, PoliticasDeLimite politicas,
            HandlerExceptionResolver erros) {
        this.baldes = baldes;
        this.geral = Politica.de("geral", politicas.geral());
        this.exportacao = Politica.de("exportacao", politicas.exportacao());
        this.erros = erros;
    }

    // Só a API: a documentação, o catálogo de problemas e o actuator ficam de fora.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().matches("/v[^/]+/.*");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var politicas = politicasDe(request);
        var cliente = cliente(request);
        var resultados = new ArrayList<ConsumptionProbe>();
        try {
            for (var politica : politicas) {
                resultados.add(balde(politica, cliente).tryConsumeAndReturnRemaining(1));
            }
        }
        catch (RuntimeException ex) {
            // Redis fora do ar: a API continua atendendo, sem limite (fail-open). A alternativa, recusar tudo,
            // transformaria uma falha do contador numa queda da API inteira.
            log.warn("Rate limiting indisponível; requisição atendida sem limite: {}", ex.toString());
            chain.doFilter(request, response);
            return;
        }

        response.setHeader(RATE_LIMIT_POLICY, String.join(", ", politicas.stream().map(Politica::descricao).toList()));
        response.setHeader(RATE_LIMIT, restante(politicas, resultados));
        for (int i = 0; i < politicas.size(); i++) {
            if (!resultados.get(i).isConsumed()) {
                var espera = segundos(resultados.get(i).getNanosToWaitForRefill());
                response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(espera));
                erros.resolveException(request, response, null, new RegraDeNegocioException(
                        TipoProblema.LIMITE_DE_REQUISICOES, Map.of("politica", politicas.get(i).nome()),
                        politicas.get(i).capacidade(), politicas.get(i).janelaEmSegundos(), espera));
                return;
            }
        }
        chain.doFilter(request, response);
    }

    // O balde fica no Redis; o proxy guarda só a otimização. Com batching, requisições simultâneas do mesmo cliente
    // nesta instância viram um único compare-and-swap no Redis. Sem ela, cinquenta requisições ao mesmo tempo
    // disputam o mesmo CAS, estouram o timeout e passam pelo fail-open, sem limite nenhum. Por isso o proxy é
    // reaproveitado: os 10 mil clientes mais recentes ficam em memória.
    private BucketProxy balde(Politica politica, String cliente) {
        var chave = "limite:%s:%s".formatted(politica.nome(), cliente);
        return proxies.computeIfAbsent(chave, _ -> baldes.getObject().builder()
                .withOptimization(Optimizations.batching())
                .build(chave.getBytes(StandardCharsets.UTF_8), politica::configuracao));
    }

    private List<Politica> politicasDe(HttpServletRequest request) {
        return request.getRequestURI().matches("/v[^/]+/produtos/exportacao")
                ? List.of(geral, exportacao)
                : List.of(geral);
    }

    // O endereço de quem abriu a conexão. Atrás de um proxy, é o do proxy: com server.forward-headers-strategy, o
    // Tomcat troca pelo do X-Forwarded-For, desde que o proxy seja confiável (29.01).
    static String cliente(HttpServletRequest request) {
        return "ip:" + chaveDoEndereco(request.getRemoteAddr());
    }

    // Um provedor entrega a cada cliente IPv6 um bloco /64 inteiro: 2^64 endereços. Limitar por endereço não segura
    // ninguém; o /64 é o equivalente a um IPv4.
    static String chaveDoEndereco(String endereco) {
        if (!(InetAddress.ofLiteral(endereco) instanceof Inet6Address ipv6)) {
            return endereco;
        }
        var bytes = Arrays.copyOf(ipv6.getAddress(), 16);
        Arrays.fill(bytes, 8, 16, (byte) 0);
        try {
            return InetAddress.getByAddress(bytes).getHostAddress() + "/64";
        }
        catch (UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // r: fichas que sobraram. t: segundos até o balde encher de novo. Com o reabastecimento contínuo, o cliente
    // ganha fichas durante t, então r é o mínimo garantido, não o máximo.
    private static String restante(List<Politica> politicas, List<ConsumptionProbe> resultados) {
        var itens = new ArrayList<String>();
        for (int i = 0; i < politicas.size(); i++) {
            itens.add("\"%s\";r=%d;t=%d".formatted(politicas.get(i).nome(), resultados.get(i).getRemainingTokens(),
                    segundos(resultados.get(i).getNanosToWaitForReset())));
        }
        return String.join(", ", itens);
    }

    private static long segundos(long nanos) {
        return Math.max(1, (nanos + TimeUnit.SECONDS.toNanos(1) - 1) / TimeUnit.SECONDS.toNanos(1));
    }
}
