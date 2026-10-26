package com.bazar.catalogo.api;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * O ETag das categorias pelo hash do corpo (20.02). O filtro guarda a resposta inteira na memória, calcula o MD5 e,
 * se ele bater com o If-None-Match, troca o 200 por um 304 sem corpo. Economiza a transferência, mas não o trabalho:
 * a consulta e a serialização acontecem do mesmo jeito. Por isso, só nas categorias, que são poucas e pequenas.
 */
@Configuration(proxyBeanMethods = false)
class EtagDasCategorias {

    @Bean
    FilterRegistrationBean<ShallowEtagHeaderFilter> etagPeloCorpoDasCategorias() {
        var registro = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
        // "/v1/categorias/*" também cobre o próprio "/v1/categorias" (a lista).
        registro.addUrlPatterns("/v1/categorias/*");
        return registro;
    }
}
