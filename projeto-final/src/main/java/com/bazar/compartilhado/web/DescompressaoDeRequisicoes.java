package com.bazar.compartilhado.web;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Requisições com corpo comprimido (Content-Encoding: gzip), descomprimidas antes de chegar ao resto da aplicação
 * (22.03). O Tomcat comprime respostas, mas não descomprime requisições.
 * <p>
 * O limite vale para os bytes descomprimidos: gzip comprime zeros 1000 para 1, e 10 MB comprimidos podem virar 10 GB
 * (uma "bomba de descompressão"). Uma codificação desconhecida é 415, com o Accept-Encoding dizendo qual é aceita
 * (RFC 9110, §12.5.3).
 */
public class DescompressaoDeRequisicoes extends OncePerRequestFilter {

    private final DataSize limite;
    private final HandlerExceptionResolver erros;

    public DescompressaoDeRequisicoes(DataSize limite, HandlerExceptionResolver erros) {
        this.limite = limite;
        this.erros = erros;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var codificacao = request.getHeader(HttpHeaders.CONTENT_ENCODING);
        if (codificacao == null || codificacao.equalsIgnoreCase("identity")) {
            chain.doFilter(request, response);
            return;
        }
        // O multipart é lido pelo Tomcat direto do socket, sem passar por este wrapper: comprimido, não daria certo.
        if (!codificacao.trim().equalsIgnoreCase("gzip") || ehMultipart(request)) {
            response.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
            erros.resolveException(request, response, null,
                    new RegraDeNegocioException(TipoProblema.CODIFICACAO_NAO_SUPORTADA, Map.of(), codificacao));
            return;
        }
        try {
            chain.doFilter(new Descomprimida(request, limite.toBytes()), response);
        }
        catch (ServletException ex) {
            // Um gzip corrompido aparece como erro de leitura no meio do processamento.
            if (ex.getCause() instanceof RegraDeNegocioException excecao) {
                erros.resolveException(request, response, null, excecao);
                return;
            }
            throw ex;
        }
    }

    private static boolean ehMultipart(HttpServletRequest request) {
        var tipo = request.getContentType();
        return tipo != null && tipo.toLowerCase(Locale.ROOT).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
    }

    /** A requisição como se tivesse chegado sem compressão: sem Content-Encoding e sem Content-Length. */
    static final class Descomprimida extends HttpServletRequestWrapper {

        private final long limite;
        private @Nullable ServletInputStream entrada;

        Descomprimida(HttpServletRequest request, long limite) {
            super(request);
            this.limite = limite;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (entrada == null) {
                var descomprimida = new ComLimite(new GZIPInputStream(super.getInputStream()), limite);
                entrada = new ServletInputStream() {

                    private boolean fim;

                    @Override
                    public int read() throws IOException {
                        int b = descomprimida.read();
                        fim = b < 0;
                        return b;
                    }

                    @Override
                    public int read(byte[] destino, int inicio, int quantos) throws IOException {
                        int n = descomprimida.read(destino, inicio, quantos);
                        fim = n < 0;
                        return n;
                    }

                    @Override
                    public boolean isFinished() {
                        return fim;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(ReadListener ouvinte) {
                        throw new UnsupportedOperationException("Leitura assíncrona de corpo comprimido");
                    }
                };
            }
            return entrada;
        }

        @Override
        public int getContentLength() {
            return -1;
        }

        @Override
        public long getContentLengthLong() {
            return -1;
        }

        @Override
        public @Nullable String getHeader(String nome) {
            if (HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(nome) || HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(nome)) {
                return null;
            }
            return super.getHeader(nome);
        }

        @Override
        public Enumeration<String> getHeaders(String nome) {
            if (HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(nome) || HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(nome)) {
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(nome);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(Collections.list(super.getHeaderNames()).stream()
                    .filter(nome -> !HttpHeaders.CONTENT_ENCODING.equalsIgnoreCase(nome)
                            && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(nome))
                    .toList());
        }
    }

    /** Conta os bytes descomprimidos e para no limite: 413. */
    static final class ComLimite extends FilterInputStream {

        private final long limite;
        private long lidos;

        ComLimite(InputStream entrada, long limite) {
            super(entrada);
            this.limite = limite;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                contar(1);
            }
            return b;
        }

        @Override
        public int read(byte[] destino, int inicio, int quantos) throws IOException {
            int n = super.read(destino, inicio, quantos);
            if (n > 0) {
                contar(n);
            }
            return n;
        }

        private void contar(int n) {
            lidos += n;
            if (lidos > limite) {
                throw new RegraDeNegocioException(TipoProblema.CORPO_GRANDE_DEMAIS, Map.of(), limite / 1024 / 1024);
            }
        }
    }
}
