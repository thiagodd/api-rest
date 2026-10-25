package com.bazar.estoque.dominio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EstoqueRepository extends JpaRepository<Estoque, UUID> {
}
