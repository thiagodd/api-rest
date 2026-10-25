package com.bazar.compartilhado.web;

import com.bazar.compartilhado.Canal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Registra as chamadas a endpoints anotados com @Auditavel. Roda dentro do DispatcherServlet,
 * então sabe qual método atendeu e com que status a resposta terminou.
 */
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final Logger auditoria = LoggerFactory.getLogger("bazar.auditoria");

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) {
        // O handler pode não ser um método de controller (um recurso estático, por exemplo).
        if (!(handler instanceof HandlerMethod metodo)) {
            return;
        }
        var auditavel = metodo.getMethodAnnotation(Auditavel.class);
        if (auditavel == null) {
            return;
        }
        auditoria.info("acao={} metodo={} caminho={} status={} canal={}", auditavel.value(), request.getMethod(),
                request.getRequestURI(), response.getStatus(), Canal.de(request.getHeader("Bazar-Canal")));
    }
}
