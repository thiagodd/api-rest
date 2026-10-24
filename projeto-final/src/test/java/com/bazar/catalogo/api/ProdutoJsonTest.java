package com.bazar.catalogo.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.StatusProduto;
import com.bazar.compartilhado.Dinheiro;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

// @JsonTest sobe só o Jackson configurado pelo Spring Boot (application.yml e @JacksonComponent).
@JsonTest
class ProdutoJsonTest {

    @Autowired
    JacksonTester<ProdutoResponse> produto;

    @Autowired
    JacksonTester<CriarProdutoRequest> criacao;

    @Autowired
    JacksonTester<Dinheiro> dinheiro;

    @Test
    void escreveOProdutoComOContratoV1() throws Exception {
        var response = new ProdutoResponse(
                UUID.fromString("0192f39f-2222-7000-8000-000000000042"), "LIV-0042", "Clean Code", null, 12990,
                UUID.fromString("0192f3a0-5555-7000-8000-000000000003"), StatusProduto.ATIVO, Map.of(),
                Instant.parse("2026-10-24T17:30:00Z"), Instant.parse("2026-10-24T17:30:00Z"));

        assertThat(produto.write(response)).isStrictlyEqualToJson("""
                { "id": "0192f39f-2222-7000-8000-000000000042", "sku": "LIV-0042", "nome": "Clean Code",
                  "descricao": null, "precoEmCentavos": 12990, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                  "status": "ATIVO", "atributos": {}, "criadoEm": "2026-10-24T17:30:00Z",
                  "atualizadoEm": "2026-10-24T17:30:00Z" }
                """);
        // a ordem dos campos é a da declaração do record
        assertThat(produto.write(response).getJson()).startsWith("{\"id\":\"0192f39f");
    }

    @Test
    void escreveODinheiroComValorEmString() throws Exception {
        assertThat(dinheiro.write(Dinheiro.deCentavos(12990, "BRL")))
                .isStrictlyEqualToJson("""
                        { "valor": "129.90", "moeda": "BRL" }
                        """);
    }

    @Test
    void ignoraPropriedadesDesconhecidasNaEntrada() throws Exception {
        var lido = criacao.parseObject("""
                { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                  "categoriaId": "0192f3a0-5555-7000-8000-000000000003", "status": "ATIVO" }
                """);

        assertThat(lido.sku()).isEqualTo("LIV-0042");
        assertThat(lido.atributos()).isEmpty();
    }
}
