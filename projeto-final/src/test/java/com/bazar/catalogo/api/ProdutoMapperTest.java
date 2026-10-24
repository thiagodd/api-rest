package com.bazar.catalogo.api;

import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.StatusProduto;
import com.bazar.compartilhado.DinheiroMapperImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Um teste unitário comum: o mapper gerado é uma classe Java, sem Spring.
class ProdutoMapperTest {

    private final ProdutoMapper mapper = new ProdutoMapperImpl(new DinheiroMapperImpl());

    @Test
    void converteOPrecoEmCentavosNosDoisSentidos() {
        var request = new CriarProdutoRequest("LIV-0042", "Clean Code", null, 12990L,
                UUID.fromString("0192f3a0-5555-7000-8000-000000000003"), Map.of("autor", "Robert C. Martin"));

        var produto = mapper.paraProduto(request);
        assertThat(produto.getPreco().valor()).isEqualByComparingTo("129.90");
        assertThat(produto.getPreco().moeda().getCurrencyCode()).isEqualTo("BRL");

        var response = mapper.paraResponse(produto);
        assertThat(response.precoEmCentavos()).isEqualTo(12990L);
        assertThat(response.status()).isEqualTo(StatusProduto.RASCUNHO);
        assertThat(response.atributos()).containsEntry("autor", "Robert C. Martin");
    }
}
