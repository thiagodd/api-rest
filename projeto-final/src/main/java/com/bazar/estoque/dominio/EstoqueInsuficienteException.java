package com.bazar.estoque.dominio;

import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;

/**
 * Uma exceção não verificada: lançada dentro de um método @Transactional, desfaz a transação inteira (10.03).
 */
public class EstoqueInsuficienteException extends RegraDeNegocioException {

    public EstoqueInsuficienteException(UUID produtoId, int disponivel, int solicitado) {
        super(TipoProblema.ESTOQUE_INSUFICIENTE,
                Map.of("produtoId", produtoId, "quantidadeDisponivel", disponivel, "quantidadeSolicitada", solicitado),
                produtoId, disponivel, solicitado);
    }
}
