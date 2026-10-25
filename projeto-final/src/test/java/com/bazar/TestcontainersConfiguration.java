package com.bazar;

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
}
