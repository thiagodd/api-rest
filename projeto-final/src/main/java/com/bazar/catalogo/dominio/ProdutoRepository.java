package com.bazar.catalogo.dominio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * O Spring Data gera a implementação na inicialização: save, findById, findAll, deleteById...
 * e as consultas derivadas dos nomes dos métodos abaixo.
 */
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    List<Produto> findAllByOrderByNomeAsc();

    long countByCategoriaId(UUID categoriaId);
}
