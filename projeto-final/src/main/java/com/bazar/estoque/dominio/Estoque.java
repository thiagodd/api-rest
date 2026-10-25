package com.bazar.estoque.dominio;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

/**
 * O saldo de um produto. Cada produto tem exatamente um estoque, criado junto com ele.
 */
@Entity
@Table(name = "estoque")
public class Estoque {

    @Id
    private UUID produtoId;

    @Column(nullable = false)
    private int quantidadeDisponivel;

    @Column(nullable = false)
    private int quantidadeReservada;

    @Version
    private @Nullable Long versao;

    protected Estoque() {
    }

    public Estoque(UUID produtoId) {
        this.produtoId = produtoId;
    }

    // Reservar tira do disponível e guarda para o pedido; a baixa definitiva acontece no envio.
    // A proteção contra duas reservas simultâneas do mesmo saldo é assunto de 10.06.
    public void reservar(int quantidade) {
        if (quantidade > quantidadeDisponivel) {
            throw new EstoqueInsuficienteException(produtoId, quantidadeDisponivel, quantidade);
        }
        quantidadeDisponivel -= quantidade;
        quantidadeReservada += quantidade;
    }

    public void repor(int quantidade) {
        quantidadeDisponivel += quantidade;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public int getQuantidadeReservada() {
        return quantidadeReservada;
    }
}
