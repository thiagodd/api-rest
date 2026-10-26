package com.bazar.compartilhado.idempotencia;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bazar.compartilhado.erros.ExcecaoDeDominio;
import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.ServletRequestPathUtils;

/**
 * Idempotency keys (21.01), no formato do Internet-Draft da IETF (draft-ietf-httpapi-idempotency-key-header). Para
 * um endpoint @Idempotente, com o header Idempotency-Key:
 * <ul>
 * <li>primeira vez: processa numa transação que também guarda a resposta (só as de sucesso, 2xx);</li>
 * <li>repetição com o mesmo corpo: devolve a resposta guardada, com Idempotent-Replayed: true;</li>
 * <li>mesma chave, outro corpo: 422;</li>
 * <li>mesma chave enquanto a primeira ainda está em andamento: 409.</li>
 * </ul>
 * A operação e a chave são gravadas na mesma transação: ou as duas existem, ou nenhuma. Uma falha no meio (erro de
 * negócio, queda do processo) desfaz tudo, e o cliente pode repetir com a mesma chave.
 * <p>
 * Registrado pela ConfiguracaoDaIdempotencia, e não como @Component: um @Component Filter entraria nos testes de
 * fatia (@WebMvcTest), que não têm banco.
 */
public class IdempotenciaFilter extends OncePerRequestFilter {

    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";
    public static final String IDEMPOTENT_REPLAYED = "Idempotent-Replayed";

    // Publicada na documentação do header, como o draft pede (21.01).
    static final Duration VALIDADE = Duration.ofHours(24);

    // Os headers que voltam numa repetição. Date, traceparent e afins são da nova resposta, não da original.
    private static final List<String> HEADERS_GUARDADOS = List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.LOCATION,
            HttpHeaders.CONTENT_LANGUAGE, HttpHeaders.CACHE_CONTROL, HttpHeaders.ETAG);

    private final RequestMappingHandlerMapping rotas;
    private final ChavesDeIdempotencia chaves;
    private final PlatformTransactionManager transacoes;
    private final HandlerExceptionResolver erros;
    private final Clock relogio;

    IdempotenciaFilter(RequestMappingHandlerMapping rotas, ChavesDeIdempotencia chaves,
            PlatformTransactionManager transacoes, HandlerExceptionResolver erros, Clock relogio) {
        this.rotas = rotas;
        this.chaves = chaves;
        this.transacoes = transacoes;
        this.erros = erros;
        this.relogio = relogio;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getHeader(IDEMPOTENCY_KEY) == null || !ehIdempotente(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var resposta = new ContentCachingResponseWrapper(response);
        try {
            processar(request, resposta, chain);
        } catch (ExcecaoDeDominio ex) {
            // O mesmo Problem Details de um erro dentro do Spring MVC (o TratadorGlobalDeErros).
            resposta.resetBuffer();
            erros.resolveException(request, resposta, null, ex);
        }
        resposta.copyBodyToResponse();
    }

    private void processar(HttpServletRequest request, ContentCachingResponseWrapper resposta, FilterChain chain)
            throws ServletException, IOException {
        var chave = lerChave(request.getHeader(IDEMPOTENCY_KEY));
        var escopo = request.getMethod() + " " + request.getRequestURI();
        var corpo = request.getInputStream().readAllBytes();
        var hash = sha256(corpo);
        var agora = relogio.instant();

        var transacao = transacoes.getTransaction(TransactionDefinition.withDefaults());
        try {
            if (!chaves.travar(escopo, chave)) {
                throw new RegraDeNegocioException(TipoProblema.REQUISICAO_EM_ANDAMENTO, Map.of());
            }
            var guardada = chaves.buscar(escopo, chave, agora);
            if (guardada.isPresent()) {
                if (!guardada.get().hashRequisicao().equals(hash)) {
                    throw new RegraDeNegocioException(TipoProblema.CHAVE_DE_IDEMPOTENCIA_REUTILIZADA, Map.of());
                }
                repetir(guardada.get(), resposta);
                transacoes.commit(transacao);
                return;
            }
            // O controller e o serviço (@Transactional) participam desta transação.
            chain.doFilter(new RequisicaoComCorpo(request, corpo), resposta);
            if (resposta.getStatus() >= 200 && resposta.getStatus() < 300) {
                chaves.guardar(escopo, chave, guardar(hash, resposta), agora, agora.plus(VALIDADE));
                transacoes.commit(transacao);
            } else {
                // Um erro (de negócio ou de validação) não deixou efeito, e não fica guardado: com a mesma chave, o
                // cliente pode repetir depois de corrigir o que faltava (o estoque, por exemplo).
                transacoes.rollback(transacao);
            }
        } catch (RuntimeException | IOException | ServletException ex) {
            if (!transacao.isCompleted()) {
                transacoes.rollback(transacao);
            }
            throw ex;
        }
    }

    // O valor é uma string de Structured Fields (RFC 9651), entre aspas. Sem aspas também é aceito, como na Stripe.
    private static String lerChave(String valor) {
        var chave = valor.strip();
        if (chave.length() >= 2 && chave.startsWith("\"") && chave.endsWith("\"")) {
            chave = chave.substring(1, chave.length() - 1);
        }
        if (chave.isBlank() || chave.length() > 255) {
            throw new RegraDeNegocioException(TipoProblema.CHAVE_DE_IDEMPOTENCIA_INVALIDA, Map.of());
        }
        return chave;
    }

    private static RespostaGuardada guardar(String hash, ContentCachingResponseWrapper resposta) {
        var headers = new LinkedHashMap<String, String>();
        for (var nome : HEADERS_GUARDADOS) {
            var valor = resposta.getHeader(nome);
            if (valor != null) {
                headers.put(nome, valor);
            }
        }
        if (resposta.getContentType() != null) {
            headers.put(HttpHeaders.CONTENT_TYPE, resposta.getContentType());
        }
        return new RespostaGuardada(hash, resposta.getStatus(), headers, resposta.getContentAsByteArray());
    }

    private static void repetir(RespostaGuardada guardada, ContentCachingResponseWrapper resposta) throws IOException {
        resposta.setStatus(guardada.status());
        guardada.headers().forEach(resposta::setHeader);
        resposta.setHeader(IDEMPOTENT_REPLAYED, "true");
        resposta.getOutputStream().write(guardada.corpo());
    }

    private boolean ehIdempotente(HttpServletRequest request) {
        try {
            if (!ServletRequestPathUtils.hasParsedRequestPath(request)) {
                ServletRequestPathUtils.parseAndCache(request);
            }
            var cadeia = rotas.getHandler(request);
            return cadeia != null && cadeia.getHandler() instanceof HandlerMethod metodo
                    && metodo.hasMethodAnnotation(Idempotente.class);
        } catch (Exception ex) {
            // Sem rota (404, 405): o Spring MVC responde o erro; a chave não importa.
            return false;
        }
    }

    private static String sha256(byte[] corpo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(corpo));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** A requisição com o corpo já lido (para o hash), para o Spring MVC ler de novo. */
    private static final class RequisicaoComCorpo extends HttpServletRequestWrapper {

        private final byte[] corpo;

        RequisicaoComCorpo(HttpServletRequest request, byte[] corpo) {
            super(request);
            this.corpo = corpo;
        }

        @Override
        public ServletInputStream getInputStream() {
            var entrada = new ByteArrayInputStream(corpo);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return entrada.read();
                }

                @Override
                public int read(byte[] destino, int inicio, int tamanho) {
                    return entrada.read(destino, inicio, tamanho);
                }

                @Override
                public boolean isFinished() {
                    return entrada.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener ouvinte) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            var charset = getCharacterEncoding() != null ? getCharacterEncoding() : StandardCharsets.UTF_8.name();
            return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(corpo),
                    Charset.forName(charset)));
        }
    }
}
