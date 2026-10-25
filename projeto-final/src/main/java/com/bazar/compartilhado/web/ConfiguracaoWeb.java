package com.bazar.compartilhado.web;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class ConfiguracaoWeb implements WebMvcConfigurer {

    // No fim da lista: com Accept */* (ou sem Accept), o primeiro conversor capaz vence, e ele deve ser o JSON.
    // Um bean HttpMessageConverter entraria no início da lista (09.03).
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ListaCsvHttpMessageConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditoriaInterceptor()).addPathPatterns("/v1/**");
        registry.addInterceptor(new ParametrosDeConsultaInterceptor()).addPathPatterns("/v1/**");
    }

    // A CURIE "bazar" das relações próprias (03.04). Aparece em _links.curies sempre que um link usa bazar:...
    @Bean
    CurieProvider curieProvider() {
        return new DefaultCurieProvider("bazar", UriTemplate.of("https://api.bazar.example/rels/{rel}"));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ContextoRequisicaoResolver());
    }
}
