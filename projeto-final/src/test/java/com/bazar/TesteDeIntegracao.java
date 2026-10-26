package com.bazar;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * A configuração única dos testes de integração: a aplicação inteira, num servidor em porta aleatória, com o
 * PostgreSQL do TestcontainersConfiguration. Todas as classes que a usam compartilham o mesmo contexto (e o
 * mesmo contêiner) no cache do Spring. Para continuar assim, não acrescente propriedades, @MockitoBean nem
 * @DirtiesContext nas classes de teste: o que for comum a todos entra aqui (11.04).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = { "spring.jpa.properties.hibernate.generate_statistics=true",
                // Os testes de webhook (21.03) entregam num servidor em localhost.
                "bazar.webhooks.aceitar-enderecos-locais=true" })
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public @interface TesteDeIntegracao {
}
