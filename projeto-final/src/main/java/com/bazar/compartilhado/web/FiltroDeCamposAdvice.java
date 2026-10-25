package com.bazar.compartilhado.web;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ser.FilterProvider;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Entrega ao conversor do Jackson 3 o filtro de ?fields= (13.05). No Spring 7, o conversor recebe o filtro como um
 * hint com a chave FilterProvider.class.getName(); o MappingJacksonValue, que fazia isso antes, está depreciado.
 */
@ControllerAdvice
class FiltroDeCamposAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter tipoDeRetorno, Class<? extends HttpMessageConverter<?>> conversor) {
        return AbstractJacksonHttpMessageConverter.class.isAssignableFrom(conversor);
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object corpo, MethodParameter tipoDeRetorno, MediaType tipo,
            Class<? extends HttpMessageConverter<?>> conversor, ServerHttpRequest requisicao,
            ServerHttpResponse resposta) {
        return corpo;
    }

    @Override
    public @Nullable Map<String, Object> determineWriteHints(@Nullable Object corpo, MethodParameter tipoDeRetorno,
            MediaType tipo, Class<? extends HttpMessageConverter<?>> conversor) {
        var filtro = CamposDaResposta.selecionado();
        return filtro == null ? null : Map.of(FilterProvider.class.getName(), filtro);
    }
}
