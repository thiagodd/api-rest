package com.bazar.estoque.dominio;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EstoqueRepository extends JpaRepository<Estoque, UUID> {

    // Conferência e baixa num único comando: duas reservas simultâneas não conseguem usar o mesmo saldo,
    // porque o PostgreSQL serializa os UPDATEs da mesma linha e reavalia o where na segunda (10.06).
    // Devolve 1 se reservou e 0 se o saldo não bastava. A versão sobe, como num UPDATE feito pelo Hibernate.
    @Modifying
    @Query("""
            update Estoque e
               set e.quantidadeDisponivel = e.quantidadeDisponivel - :quantidade,
                   e.quantidadeReservada = e.quantidadeReservada + :quantidade,
                   e.versao = e.versao + 1
             where e.produtoId = :produtoId
               and e.quantidadeDisponivel >= :quantidade""")
    int reservar(UUID produtoId, int quantidade);

    // O inverso da reserva, quando o pedido é cancelado. O where protege de liberar mais do que foi reservado.
    @Modifying
    @Query("""
            update Estoque e
               set e.quantidadeDisponivel = e.quantidadeDisponivel + :quantidade,
                   e.quantidadeReservada = e.quantidadeReservada - :quantidade,
                   e.versao = e.versao + 1
             where e.produtoId = :produtoId
               and e.quantidadeReservada >= :quantidade""")
    int liberar(UUID produtoId, int quantidade);
}
