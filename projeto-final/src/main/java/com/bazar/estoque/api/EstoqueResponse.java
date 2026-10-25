package com.bazar.estoque.api;

import java.util.UUID;

import com.bazar.estoque.dominio.Estoque;

/**
 * O saldo de um produto. A versão vai no corpo por enquanto; em 20.03, passa para o ETag.
 */
public record EstoqueResponse(UUID produtoId, int quantidadeDisponivel, int quantidadeReservada, long versao) {

    static EstoqueResponse de(Estoque estoque) {
        return new EstoqueResponse(estoque.getProdutoId(), estoque.getQuantidadeDisponivel(),
                estoque.getQuantidadeReservada(), estoque.getVersao());
    }
}
