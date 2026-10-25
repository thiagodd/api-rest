package com.bazar.pedidos.dominio;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    // O próximo sequencial do número do pedido (15.03). Uma sequence nunca devolve o mesmo valor duas vezes, mesmo
    // com pedidos simultâneos; um pedido desfeito deixa um buraco na numeração, e isso é aceitável.
    @Query(value = "select nextval('pedido_numero_seq')", nativeQuery = true)
    long proximoNumero();

    // Traz os itens na mesma consulta: com o OSIV desligado, eles não podem ser carregados depois (10.04).
    @EntityGraph(attributePaths = "itens")
    Optional<Pedido> findComItensById(UUID id);

    // Sem o @EntityGraph, seriam 1 + N consultas: uma para os pedidos e uma para os itens de cada um.
    // Desde o Hibernate 7.4, o limite é aplicado no SQL mesmo com a coleção no join (subconsulta).
    // Paginação por cursor (13.04): a primeira página, e as seguintes e anteriores a partir do (criadoEm, id) de um
    // pedido já visto. A comparação de linhas, (a, b) < (x, y), vira um limite no índice
    // (cliente_id, criado_em desc, id desc): o banco começa na posição e lê só a página. O Window do Spring Data
    // escreve a mesma condição como "a < x or (a = x and b < y)", e o PostgreSQL percorre o índice desde o início
    // do cliente, descartando cada linha antes da posição, como um offset.
    @EntityGraph(attributePaths = "itens")
    List<Pedido> findComItensByClienteIdOrderByCriadoEmDescIdDesc(UUID clienteId, Limit limite);

    @EntityGraph(attributePaths = "itens")
    @Query("""
            select p from Pedido p
            where p.clienteId = :clienteId and (p.criadoEm, p.id) < (:criadoEm, :id)
            order by p.criadoEm desc, p.id desc
            """)
    List<Pedido> buscarDepoisDe(UUID clienteId, Instant criadoEm, UUID id, Limit limite);

    // Os anteriores, do mais próximo da posição para o mais distante: a ordem inversa da listagem.
    @EntityGraph(attributePaths = "itens")
    @Query("""
            select p from Pedido p
            where p.clienteId = :clienteId and (p.criadoEm, p.id) > (:criadoEm, :id)
            order by p.criadoEm, p.id
            """)
    List<Pedido> buscarAntesDe(UUID clienteId, Instant criadoEm, UUID id, Limit limite);
}
