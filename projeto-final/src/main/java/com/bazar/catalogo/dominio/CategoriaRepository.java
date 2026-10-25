package com.bazar.catalogo.dominio;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    List<Categoria> findAllByOrderByNomeAsc();

    // Com um Pageable, o Spring Data executa esta consulta com limit/offset e deriva dela um select count(...),
    // para os totais da página (13.03). O padrão do LIKE já chega com os curingas escapados.
    @Query("""
            select c from Categoria c
            where (:apenasRaizes = false or c.categoriaPaiId is null)
              and (:padrao is null or lower(c.nome) like :padrao escape '\\')
            """)
    Page<Categoria> buscar(@Nullable String padrao, boolean apenasRaizes, Pageable pagina);

    long countByCategoriaPaiId(UUID categoriaPaiId);
}
