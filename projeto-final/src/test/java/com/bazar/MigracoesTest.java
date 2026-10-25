package com.bazar;

import java.util.UUID;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Toda execução dos testes de integração já aplica as migrations do zero num PostgreSQL novo, e o Hibernate
 * confere o esquema (ddl-auto: validate). Este teste vai além: verifica o que uma migration faz com dados que
 * já existiam. Ele migra um esquema separado até a versão anterior, insere os dados e aplica a migration.
 */
@TesteDeIntegracao
class MigracoesTest {

    static final String ESQUEMA = "teste_migracoes";

    @Autowired
    DataSource dataSource;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void apagarOEsquema() {
        jdbc.sql("drop schema if exists " + ESQUEMA + " cascade").update();
    }

    @Test
    void aV7CriaUmEstoqueZeradoParaOsProdutosQueJaExistiam() {
        flywayAte("6").migrate();
        var produtoAntigo = UUID.fromString("0192f39f-2222-7000-8000-000000000001");
        jdbc.sql("""
                insert into teste_migracoes.produto
                    (id, sku, nome, preco_valor, preco_moeda, categoria_id, status, versao, criado_em, atualizado_em)
                values (?, 'LIV-0001', 'Produto de antes da 10.03', 10.00, 'BRL',
                        '0192f3a0-5555-7000-8000-000000000003', 'ATIVO', 0, now(), now())
                """).param(produtoAntigo).update();

        var resultado = flywayAte("7").migrate();

        assertThat(resultado.migrationsExecuted).isEqualTo(1);
        assertThat(jdbc.sql("select quantidade_disponivel from teste_migracoes.estoque where produto_id = ?")
                .param(produtoAntigo).query(Integer.class).single()).isZero();
    }

    private Flyway flywayAte(String versao) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas(ESQUEMA)
                .target(versao)
                .load();
    }
}
