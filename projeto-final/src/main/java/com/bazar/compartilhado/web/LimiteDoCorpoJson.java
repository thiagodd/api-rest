package com.bazar.compartilhado.web;

import java.io.IOException;
import java.util.Map;

import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Limite de tamanho para corpos JSON (23.02). O Tomcat não limita o corpo de um POST que não seja formulário, e o
 * Jackson lê o que vier: um lote com um milhão de itens é montado inteiro na memória antes de o @Size(max = 500)
 * recusá-lo. Este filtro recusa antes.
 * <p>
 * Com Content-Length, recusa sem ler nada. Sem ele (chunked, ou depois da descompressão), conta os bytes lidos e
 * interrompe a leitura ao passar do limite.
 */
public class LimiteDoCorpoJson extends OncePerRequestFilter {

    private final long limite;
    private final HandlerExceptionResolver erros;

    public LimiteDoCorpoJson(DataSize limite, HandlerExceptionResolver erros) {
        this.limite = limite.toBytes();
        this.erros = erros;
    }

    // Só JSON: os arquivos (22.01) e o multipart têm limites próprios, maiores.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var tipo = request.getContentType();
        if (tipo == null) {
            return true;
        }
        var mediaType = MediaType.parseMediaType(tipo);
        return !mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)
                && !"json".equals(mediaType.getSubtypeSuffix());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getContentLengthLong() > limite) {
            erros.resolveException(request, response, null, grandeDemais());
            return;
        }
        try {
            chain.doFilter(new ComLimite(request), response);
        }
        catch (RegraDeNegocioException ex) {
            // Um filtro que lê o corpo antes do controller (a idempotência, 21.01) recebe a exceção sem embrulho;
            // no controller, o Jackson a embrulha, e o tratador global a desembrulha.
            erros.resolveException(request, response, null, ex);
        }
        catch (ServletException ex) {
            if (ex.getCause() instanceof RegraDeNegocioException excecao) {
                erros.resolveException(request, response, null, excecao);
                return;
            }
            throw ex;
        }
    }

    private RegraDeNegocioException grandeDemais() {
        return new RegraDeNegocioException(TipoProblema.CORPO_GRANDE_DEMAIS, Map.of(), limite / 1024 / 1024);
    }

    private final class ComLimite extends HttpServletRequestWrapper {

        private final ServletInputStream entrada;

        ComLimite(HttpServletRequest request) throws IOException {
            super(request);
            var original = request.getInputStream();
            this.entrada = new ServletInputStream() {
                private long lidos;

                @Override
                public int read() throws IOException {
                    int b = original.read();
                    if (b != -1) {
                        contar(1);
                    }
                    return b;
                }

                @Override
                public int read(byte[] destino, int inicio, int tamanho) throws IOException {
                    int n = original.read(destino, inicio, tamanho);
                    if (n > 0) {
                        contar(n);
                    }
                    return n;
                }

                private void contar(int n) {
                    lidos += n;
                    if (lidos > limite) {
                        throw grandeDemais();
                    }
                }

                @Override
                public boolean isFinished() {
                    return original.isFinished();
                }

                @Override
                public boolean isReady() {
                    return original.isReady();
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    original.setReadListener(listener);
                }
            };
        }

        @Override
        public ServletInputStream getInputStream() {
            return entrada;
        }
    }
}
