package com.bazar.catalogo.dominio;

import java.util.Comparator;
import java.util.List;
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

    public List<Produto> findAll() {
        return produtos.values().stream()
                .sorted(Comparator.comparing(Produto::getNome))
                .toList();
    }

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
