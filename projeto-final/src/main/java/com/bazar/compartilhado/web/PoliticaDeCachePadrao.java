package com.bazar.compartilhado.web;

import com.bazar.compartilhado.Lista;
import org.jspecify.annotations.Nullable;

import org.springframework.core.MethodParameter;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Os headers de cache que valem para toda a API (20.01).
 */
@RestControllerAdvice
class PoliticaDeCachePadrao implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter tipoDeRetorno, Class<? extends HttpMessageConverter<?>> conversor) {
        return true;
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object corpo, MethodParameter tipoDeRetorno,
            MediaType tipoDeConteudo, Class<? extends HttpMessageConverter<?>> conversor, ServerHttpRequest requisicao,
            ServerHttpResponse resposta) {
        var cabecalhos = resposta.getHeaders();
        // Por padrão, nenhuma resposta vai para cache: quem quer cache declara no controller. Sem Cache-Control, a
        // resposta fica à mercê da heurística de cada cache (RFC 9111, §4.2.2), e um proxy poderia guardar um 404 ou
        // os pedidos de um cliente.
        if (!cabecalhos.containsHeader(HttpHeaders.CACHE_CONTROL)) {
            cabecalhos.setCacheControl(CacheControl.noStore());
        }
        // Uma Lista sai em JSON ou CSV pela mesma URI, conforme o Accept (09.03): um cache precisa guardar uma cópia
        // por formato, ou entrega CSV a quem pediu JSON.
        if (corpo instanceof Lista<?> && !cabecalhos.getVary().contains(HttpHeaders.ACCEPT)) {
            cabecalhos.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
        }
        return corpo;
    }
}
