package com.bazar.compartilhado.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Mede o tempo total de cada requisição, inclusive o que acontece antes e depois do Spring MVC.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)   // logo depois do characterEncodingFilter
public class TempoDeRespostaFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TempoDeRespostaFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long inicio = System.nanoTime();
        try {
            chain.doFilter(request, response);
        }
        finally {
            // finally: mede também as requisições que terminam em exceção.
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            log.info("{} {} → {} em {} ms", request.getMethod(), request.getRequestURI(), response.getStatus(), ms);
        }
    }
}
