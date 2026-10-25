package com.bazar.pedidos.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    // Traz os itens na mesma consulta: com o OSIV desligado, eles não podem ser carregados depois (10.04).
    @EntityGraph(attributePaths = "itens")
    Optional<Pedido> findComItensById(UUID id);

    // Sem o @EntityGraph, seriam 1 + N consultas: uma para os pedidos e uma para os itens de cada um.
    // Desde o Hibernate 7.4, o limite é aplicado no SQL mesmo com a coleção no join (subconsulta).
    @EntityGraph(attributePaths = "itens")
    List<Pedido> findComItensByClienteIdOrderByCriadoEmDesc(UUID clienteId, Limit limite);
}
