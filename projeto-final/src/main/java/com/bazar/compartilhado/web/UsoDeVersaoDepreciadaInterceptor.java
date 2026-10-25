package com.bazar.compartilhado.web;

import com.bazar.compartilhado.Canal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.web.accept.ApiVersionHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Registra quem ainda usa uma versão depreciada (18.04): a resposta saiu com o header Deprecation. Antes de desligar a
 * v1, o Bazar precisa saber quais canais e clientes ainda a chamam. Em 27.05, vira uma métrica com a versão como tag.
 */
class UsoDeVersaoDepreciadaInterceptor implements HandlerInterceptor {

    private static final Logger uso = LoggerFactory.getLogger("bazar.depreciacao");

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) {
        if (response.getHeader("Deprecation") == null) {
            return;
        }
        var versao = request.getAttribute(HandlerMapping.API_VERSION_ATTRIBUTE) instanceof ApiVersionHolder holder
                ? holder.getVersionIfPresent()
                : null;
        uso.info("versao={} operacao=\"{} {}\" canal={} agente=\"{}\"", versao, request.getMethod(),
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE),
                Canal.de(request.getHeader(ContextoRequisicaoResolver.CABECALHO_CANAL)),
                request.getHeader(HttpHeaders.USER_AGENT));
    }
}
