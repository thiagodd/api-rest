package com.bazar.catalogo.dominio;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Uma categoria do catálogo. Pode ter uma categoria pai (Livros → Programação).
 */
public record Categoria(UUID id, String nome, String slug, @Nullable UUID categoriaPaiId) {
}
