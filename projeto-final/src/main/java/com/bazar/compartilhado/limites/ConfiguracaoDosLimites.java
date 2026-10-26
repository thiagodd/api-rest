package com.bazar.compartilhado.limites;

import java.time.Duration;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PoliticasDeLimite.class)
class ConfiguracaoDosLimites {

    private static final Duration TEMPO_MAXIMO_NO_REDIS = Duration.ofMillis(200);

    // Um cliente Redis só dos limites, com a conexão que o Spring Boot descobriu (Docker Compose, Testcontainers ou
    // spring.data.redis.*). Não dá para reaproveitar o da LettuceConnectionFactory: ela é um Lifecycle, e um stop()
    // do contexto fecha o cliente, deixando o Bucket4j com uma conexão morta. O timeout curto é o tempo que uma
    // requisição espera antes do fail-open, com o Redis fora do ar.
    @Bean(destroyMethod = "shutdown")
    RedisClient clienteRedisDosLimites(DataRedisConnectionDetails conexao) {
        var endereco = RedisURI.builder()
                .withHost(conexao.getStandalone().getHost())
                .withPort(conexao.getStandalone().getPort())
                .withTimeout(TEMPO_MAXIMO_NO_REDIS);
        if (conexao.getPassword() != null) {
            endereco.withPassword(conexao.getPassword().toCharArray());
        }
        return RedisClient.create(endereco.build());
    }

    // Os baldes no Redis. Cada balde expira quando ficaria cheio de novo: um cliente que some não deixa chave para
    // trás, e um balde cheio é igual a um balde que não existe. @Lazy: o Bucket4j conecta ao Redis já na construção,
    // e sem o @Lazy a aplicação nem subiria com o Redis fora do ar. Criado no primeiro uso, e tentado de novo a cada
    // requisição enquanto falhar (o Spring não guarda um singleton que falhou).
    @Bean
    @Lazy
    ProxyManager<byte[]> baldesDeLimite(RedisClient clienteRedisDosLimites) {
        return Bucket4jLettuce.casBasedBuilder(clienteRedisDosLimites)
                .requestTimeout(TEMPO_MAXIMO_NO_REDIS)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                        Duration.ofSeconds(10)))
                .build();
    }

    // Depois do RequestContextFilter (-105), que define o idioma das mensagens, e antes da descompressão (-100) e de
    // tudo que lê o corpo: recusar uma requisição custa só a ida ao Redis.
    @Bean
    FilterRegistrationBean<LimiteDeRequisicoesFilter> limiteDeRequisicoes(ObjectProvider<ProxyManager<byte[]>> baldes,
            PoliticasDeLimite politicas, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver erros) {
        var registro = new FilterRegistrationBean<>(new LimiteDeRequisicoesFilter(baldes, politicas, erros));
        registro.setOrder(-102);
        return registro;
    }
}
