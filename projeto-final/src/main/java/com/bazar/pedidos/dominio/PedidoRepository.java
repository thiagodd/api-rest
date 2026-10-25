package com.bazar.pedidos.dominio;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    // Traz os itens na mesma consulta: com o OSIV desligado, eles não podem ser carregados depois (10.04).
    @EntityGraph(attributePaths = "itens")
    Optional<Pedido> findComItensById(UUID id);
}
