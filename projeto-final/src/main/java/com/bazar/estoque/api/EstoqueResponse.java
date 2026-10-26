package com.bazar.estoque.api;

import java.util.UUID;

import com.bazar.estoque.dominio.Estoque;

/**
 * O saldo de um produto. A versão também vai no ETag (20.03); no corpo, fica pelos clientes da 10.06.
 */
public record EstoqueResponse(UUID produtoId, int quantidadeDisponivel, int quantidadeReservada, long versao) {

    static EstoqueResponse de(Estoque estoque) {
        return new EstoqueResponse(estoque.getProdutoId(), estoque.getQuantidadeDisponivel(),
                estoque.getQuantidadeReservada(), estoque.getVersao());
    }
}
