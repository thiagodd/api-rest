package com.bazar.compartilhado;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O relógio do Bazar (15.01). Quem precisa de "agora" recebe um Clock em vez de chamar Instant.now(): nos testes,
 * um Clock.fixed torna a hora previsível. Em UTC, como todo instante que o Bazar guarda e devolve.
 */
@Configuration(proxyBeanMethods = false)
public class ConfiguracaoDoRelogio {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
