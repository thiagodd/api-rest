package com.bazar.estoque.dominio;

import java.util.UUID;

import com.bazar.compartilhado.erros.ConflitoDeVersaoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstoqueService {

    private final EstoqueRepository estoques;

    public EstoqueService(EstoqueRepository estoques) {
        this.estoques = estoques;
    }

    @Transactional(readOnly = true)
    public Estoque buscar(UUID produtoId) {
        return estoques.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("produto-nao-encontrado", produtoId));
    }

    // Locking otimista em duas camadas: a versão que o cliente leu é conferida aqui; a corrida entre esta
    // conferência e o commit é pega pelo @Version no UPDATE (OptimisticLockingFailureException → 409).
    @Transactional
    public Estoque definirDisponivel(UUID produtoId, int quantidade, long versaoLida) {
        var estoque = buscar(produtoId);
        if (estoque.getVersao() != versaoLida) {
            throw new ConflitoDeVersaoException(estoque.getVersao());
        }
        estoque.definirDisponivel(quantidade);
        return estoque;
    }

    // Participa da transação de quem chama (o PedidoService): se o pedido for desfeito, a reserva também é.
    @Transactional
    public void reservar(UUID produtoId, int quantidade) {
        if (estoques.reservar(produtoId, quantidade) == 0) {
            var estoque = buscar(produtoId);
            throw new EstoqueInsuficienteException(produtoId, estoque.getQuantidadeDisponivel(), quantidade);
        }
    }
}
