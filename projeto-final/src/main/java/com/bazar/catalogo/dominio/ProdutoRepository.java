package com.bazar.catalogo.dominio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * O Spring Data gera a implementação na inicialização: save, findById, findAll, deleteById...
 * e as consultas derivadas dos nomes dos métodos abaixo. Com JpaSpecificationExecutor, também
 * consultas montadas a partir de condições (ProdutoSpecifications, 10.05).
 */
public interface ProdutoRepository extends JpaRepository<Produto, UUID>, JpaSpecificationExecutor<Produto> {

    long countByCategoriaId(UUID categoriaId);
}
