package com.bazar.compartilhado.web;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import tools.jackson.databind.ser.std.SimpleFilterProvider;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.RequestPath;
import org.springframework.util.Assert;
import org.springframework.web.accept.StandardApiVersionDeprecationHandler;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class ConfiguracaoWeb implements WebMvcConfigurer {

    // Todo primeiro segmento que começa com v é a versão, válida ou não: com /v\d+/, um /v1.5/produtos ficaria sem
    // versão e o {versao} do mapeamento aceitaria qualquer valor (200 em vez de 400).
    // Cada exportação ocupa uma conexão do banco durante todo o download: o limite protege o pool (28.02).
    static final int RESPOSTAS_ASSINCRONAS_SIMULTANEAS = 8;

    private static final Pattern VERSAO_NO_CAMINHO = Pattern.compile("/v[^/]*/");

    // No fim da lista: com Accept */* (ou sem Accept), o primeiro conversor capaz vence, e ele deve ser o JSON.
    // Um bean HttpMessageConverter entraria no início da lista (09.03).
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new ListaCsvHttpMessageConverter());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /v*/**: todas as versões (18.03). Com /v1/**, a v2 ficaria sem auditoria e sem a recusa de parâmetros
        // desconhecidos.
        registry.addInterceptor(new AuditoriaInterceptor()).addPathPatterns("/v*/**");
        registry.addInterceptor(new ParametrosDeConsultaInterceptor()).addPathPatterns("/v*/**");
        registry.addInterceptor(new UsoDeVersaoDepreciadaInterceptor()).addPathPatterns("/v*/**");
    }

    // As respostas assíncronas do MVC (StreamingResponseBody, 22.02) num executor só delas. Sem isso, usariam o
    // executor do @Async (19.03), limitado a 4 e com recusa: a quinta exportação simultânea seria um 500, e quatro
    // exportações tomariam todas as vagas dos relatórios. Acima do limite, a recusa vira 503 com Retry-After.
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        var executor = new SimpleAsyncTaskExecutor("resposta-");
        executor.setVirtualThreads(true);
        executor.setConcurrencyLimit(RESPOSTAS_ASSINCRONAS_SIMULTANEAS);
        executor.setRejectTasksWhenLimitReached(true);
        // O MDC (traceId) da requisição vai junto para a thread que escreve a resposta.
        executor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        configurer.setTaskExecutor(executor);
    }

    // A versão é o primeiro segmento do caminho (/v1, /v2), conforme o ADR-0003 (18.03). O predicado separa as rotas
    // da API das outras: sem ele, o "problemas" de /problemas/... seria lido como versão e recusado. O /v3/api-docs
    // passa no predicado, mas o springdoc protege os próprios caminhos da validação de versão.
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.usePathSegment(0, ConfiguracaoWeb::rotaDaApi)
                .addSupportedVersions("1", "2")
                // Obrigatória por padrão: sem isto, /problemas/... (sem versão no caminho) seria um 400.
                .setVersionRequired(false)
                .setDeprecationHandler(depreciacoes());
    }

    // Os headers Deprecation, Sunset e Link (18.04), a partir do calendário de depreciação. O handler padrão guarda
    // uma entrada por versão: dois recursos depreciando a mesma versão sobrescreveriam um ao outro.
    static StandardApiVersionDeprecationHandler depreciacoes() {
        var versoes = Depreciacao.CALENDARIO.stream().map(Depreciacao::versao).toList();
        Assert.state(versoes.size() == Set.copyOf(versoes).size(),
                "Uma depreciação por versão: o StandardApiVersionDeprecationHandler guarda uma entrada por versão");
        var handler = new StandardApiVersionDeprecationHandler();
        for (var depreciacao : Depreciacao.CALENDARIO) {
            handler.configureVersion(depreciacao.versao())
                    .setRequestPredicate(request -> depreciacao.vale(request.getRequestURI()))
                    .setDeprecationDate(depreciacao.inicio())
                    .setDeprecationLink(depreciacao.guia())
                    .setSunsetDate(depreciacao.fim())
                    .setSunsetLink(Depreciacao.POLITICA);
        }
        return handler;
    }

    static boolean rotaDaApi(RequestPath caminho) {
        var valor = caminho.pathWithinApplication().value();
        return VERSAO_NO_CAMINHO.matcher(valor).lookingAt();
    }

    // O nome localeResolver substitui o LocaleResolver do Spring Boot (15.05).
    @Bean
    LocaleResolver localeResolver() {
        return new ResolvedorDeIdioma();
    }

    // A CURIE "bazar" das relações próprias (03.04). Aparece em _links.curies sempre que um link usa bazar:...
    @Bean
    CurieProvider curieProvider() {
        return new DefaultCurieProvider("bazar", UriTemplate.of("https://api.bazar.example/rels/{rel}"));
    }

    // Um filtro padrão que não filtra nada: um tipo com @JsonFilter (ProdutoResumoResponse) sai completo quando a
    // requisição não pede fields. Sem isso, o Jackson falha ao não achar o filtro (13.05).
    @Bean
    JsonMapperBuilderCustomizer filtroDeCamposPadrao() {
        return builder -> builder.filterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ContextoRequisicaoResolver());
    }
}
