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

    public void repor(int quantidade) {
        quantidadeDisponivel += quantidade;
    }

    // A reserva não passa por aqui: é um UPDATE atômico no EstoqueRepository (10.06).
    public void definirDisponivel(int quantidade) {
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade negativa: " + quantidade);
        }
        quantidadeDisponivel = quantidade;
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

    public long getVersao() {
        return versao == null ? 0 : versao;
    }
}
