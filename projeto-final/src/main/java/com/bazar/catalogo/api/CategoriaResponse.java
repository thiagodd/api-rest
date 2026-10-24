package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.Categoria;
import org.jspecify.annotations.Nullable;

public record CategoriaResponse(UUID id, String nome, String slug, @Nullable UUID categoriaPaiId) {

    static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.id(), categoria.nome(), categoria.slug(), categoria.categoriaPaiId());
    }
}
