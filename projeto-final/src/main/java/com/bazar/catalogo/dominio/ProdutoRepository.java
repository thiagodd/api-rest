package com.bazar.catalogo.dominio;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * Produtos guardados em memória até a Parte 4 (10.01).
 */
@Repository
public class ProdutoRepository {

    private final Map<UUID, Produto> produtos = new ConcurrentHashMap<>();

    public Optional<Produto> findById(UUID id) {
        return Optional.ofNullable(produtos.get(id));
    }

    public long countByCategoriaId(UUID categoriaId) {
        return produtos.values().stream().filter(p -> p.getCategoriaId().equals(categoriaId)).count();
    }

    public Produto save(Produto produto) {
        produtos.put(produto.getId(), produto);
        return produto;
    }
}
