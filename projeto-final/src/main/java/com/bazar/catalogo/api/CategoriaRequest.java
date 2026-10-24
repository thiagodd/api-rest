package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Corpo de POST e de PUT em /v1/categorias. As duas operações recebem os mesmos campos,
 * porque o PUT substitui a categoria inteira.
 */
public record CategoriaRequest(
        @NotBlank @Size(max = 60) String nome,
        @NotBlank @Size(max = 60) @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*") String slug,
        @Nullable UUID categoriaPaiId) {

    Categoria paraCategoria(UUID id) {
        return new Categoria(id, nome, slug, categoriaPaiId);
    }
}
