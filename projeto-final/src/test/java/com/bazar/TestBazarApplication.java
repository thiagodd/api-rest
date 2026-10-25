package com.bazar;

import org.springframework.boot.SpringApplication;

/**
 * Sobe o Bazar no desenvolvimento com o PostgreSQL dos testes (TestcontainersConfiguration), sem compose.yaml:
 * {@code ./mvnw spring-boot:test-run}. O banco nasce vazio a cada execução e some quando a aplicação para.
 */
public class TestBazarApplication {

    public static void main(String[] args) {
        // Sem isto, o spring-boot-docker-compose também sobe o compose.yaml, e a aplicação se conecta ao banco
        // dele: o contêiner do Testcontainers fica rodando sem uso (verificado na 11.04).
        System.setProperty("spring.docker.compose.enabled", "false");
        SpringApplication.from(BazarApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
