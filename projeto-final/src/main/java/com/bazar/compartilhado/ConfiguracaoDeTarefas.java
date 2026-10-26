package com.bazar.compartilhado;

import java.lang.reflect.Method;
import java.util.Arrays;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.integration.Slf4jThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tarefas fora da requisição (19.03): @Async no executor de tarefas do Boot (virtual threads, 19.02) e @Scheduled.
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableScheduling
public class ConfiguracaoDeTarefas implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracaoDeTarefas.class);

    static {
        // O MDC inteiro passa da thread que chama o @Async para a que o executa. A propagação em si é ligada por
        // spring.task.execution.propagate-context; o registro diz o que propagar.
        ContextRegistry.getInstance().registerThreadLocalAccessor(new Slf4jThreadLocalAccessor());
    }

    // Exceções de métodos @Async que devolvem void não têm quem as receba: sem este handler, só um log genérico.
    // Os que devolvem CompletableFuture entregam a exceção no futuro.
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method metodo, Object... argumentos) -> log.error("Falha em {}.{}({})",
                metodo.getDeclaringClass().getSimpleName(), metodo.getName(), Arrays.toString(argumentos), ex);
    }
}
