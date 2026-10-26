package com.bazar.reativo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import io.r2dbc.spi.Readable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/produtos")
class ProdutoController {

    private static final String COLUNAS = """
            select id, sku, nome, descricao, preco_valor, preco_moeda, categoria_id, status, atualizado_em
              from produto
            """;

    private final DatabaseClient banco;

    ProdutoController(DatabaseClient banco) {
        this.banco = banco;
    }

    // O método devolve na hora um Mono vazio de dados: a consulta só roda quando o WebFlux assina o Mono para
    // escrever a resposta, e nenhuma thread espera o banco responder.
    @GetMapping("/{id}")
    Mono<ProdutoResponse> buscar(@PathVariable UUID id) {
        return banco.sql(COLUNAS + " where id = :id")
                .bind("id", id)
                .map(ProdutoController::paraResposta)
                .one()
                .switchIfEmpty(Mono.error(() -> new ProdutoNaoEncontradoException(id)));
    }

    // Um Flux: os produtos saem um por linha (NDJSON) à medida que o banco os entrega, e o ritmo é o do cliente.
    // Se ele lê devagar, o WebFlux pede menos linhas ao driver (backpressure), em vez de acumulá-las na memória.
    @GetMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    Flux<ProdutoResponse> listar() {
        return banco.sql(COLUNAS + " order by nome, id")
                .map(ProdutoController::paraResposta)
                .all();
    }

    private static ProdutoResponse paraResposta(Readable linha) {
        return new ProdutoResponse(linha.get("id", UUID.class), linha.get("sku", String.class),
                linha.get("nome", String.class), linha.get("descricao", String.class),
                new ProdutoResponse.Preco(linha.get("preco_valor", BigDecimal.class),
                        linha.get("preco_moeda", String.class)),
                linha.get("categoria_id", UUID.class), linha.get("status", String.class),
                linha.get("atualizado_em", OffsetDateTime.class).toInstant());
    }
}
