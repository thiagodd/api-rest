package com.bazar.compartilhado.web;

import com.bazar.compartilhado.Canal;
import org.jspecify.annotations.Nullable;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class ContextoRequisicaoResolver implements HandlerMethodArgumentResolver {

    static final String CABECALHO_CANAL = "Bazar-Canal";

    @Override
    public boolean supportsParameter(MethodParameter parametro) {
        return parametro.getParameterType().equals(ContextoRequisicao.class);
    }

    @Override
    public ContextoRequisicao resolveArgument(MethodParameter parametro, @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest request, @Nullable WebDataBinderFactory binderFactory) {
        // O idioma já foi resolvido pelo LocaleResolver (Accept-Language, com pt-BR como padrão).
        return new ContextoRequisicao(Canal.de(request.getHeader(CABECALHO_CANAL)), LocaleContextHolder.getLocale());
    }
}
