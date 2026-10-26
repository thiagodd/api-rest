package com.bazar.reativo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * O catálogo do Bazar em WebFlux, só para comparar com a versão Spring MVC (19.05). Lê a tabela produto do mesmo
 * PostgreSQL que o Bazar usa.
 */
@SpringBootApplication
public class CatalogoReativoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoReativoApplication.class, args);
    }
}
