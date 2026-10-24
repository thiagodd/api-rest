package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.Categoria;
import org.jspecify.annotations.Nullable;

/**
 * Corpo de POST e de PUT em /v1/categorias. As duas operações recebem os mesmos campos,
 * porque o PUT substitui a categoria inteira.
 */
public record CategoriaRequest(String nome, String slug, @Nullable UUID categoriaPaiId) {

    Categoria paraCategoria(UUID id) {
        return new Categoria(id, nome, slug, categoriaPaiId);
    }
}
