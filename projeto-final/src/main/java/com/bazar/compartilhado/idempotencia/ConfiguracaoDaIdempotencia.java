package com.bazar.compartilhado.idempotencia;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration(proxyBeanMethods = false)
class ConfiguracaoDaIdempotencia {

    @Bean
    FilterRegistrationBean<IdempotenciaFilter> idempotencia(RequestMappingHandlerMapping rotas,
            ChavesDeIdempotencia chaves, PlatformTransactionManager transacoes,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver erros, Clock relogio) {
        // Em todas as rotas: o filtro mesmo descarta, sem custo, as requisições sem Idempotency-Key.
        return new FilterRegistrationBean<>(new IdempotenciaFilter(rotas, chaves, transacoes, erros, relogio));
    }
}
