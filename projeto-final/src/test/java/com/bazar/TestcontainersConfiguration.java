package com.bazar;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

/**
 * Um PostgreSQL de verdade, num contêiner, para os testes que usam o banco e para o TestBazarApplication.
 * O @ServiceConnection entrega a URL, o usuário e a senha do contêiner ao Spring Boot.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    // @RestartScope: quando o DevTools reinicia a aplicação (TestBazarApplication), o contêiner e os dados ficam.
    @Bean
    @RestartScope
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18");
    }

    // O Redis dos baldes do rate limiting (23.01). O name diz ao @ServiceConnection que tipo de serviço é.
    @Bean
    @RestartScope
    @ServiceConnection(name = "redis")
    GenericContainer<?> redis() {
        return new GenericContainer<>("redis:8").withExposedPorts(6379);
    }
}
