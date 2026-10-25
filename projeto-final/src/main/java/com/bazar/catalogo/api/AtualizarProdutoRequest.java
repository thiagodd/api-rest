package com.bazar.catalogo.api;

import com.bazar.catalogo.dominio.Produto;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Corpo de PUT /v1/produtos/{id}. O SKU não aparece: ele não muda depois da criação.
 */
public record AtualizarProdutoRequest(
        @NotBlank @Size(max = 120) String nome,
        @Nullable @Size(max = 2000) String descricao,
        @NotNull @Positive Long precoEmCentavos,
        @NotNull UUID categoriaId,
        @Size(max = 20) Map<String, String> atributos) {

    public AtualizarProdutoRequest {
        atributos = atributos == null ? Map.of() : Map.copyOf(atributos); // ausente no JSON → vazio
    }

    // A parte editável de um produto: o documento a que um PATCH se aplica (14.02).
    static AtualizarProdutoRequest de(Produto produto) {
        return new AtualizarProdutoRequest(produto.getNome(), produto.getDescricao(), produto.getPreco().emCentavos(),
                produto.getCategoriaId(), produto.getAtributos());
    }
}
