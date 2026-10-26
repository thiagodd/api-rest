package com.bazar.estoque.dominio;

import java.util.UUID;
import java.util.function.LongPredicate;

import com.bazar.compartilhado.erros.ConflitoDeVersaoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;

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
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.PRODUTO_NAO_ENCONTRADO, produtoId));
    }

    // Locking otimista em duas camadas: a versão que o cliente leu é conferida aqui (no corpo, 10.06, ou no
    // If-Match, 20.03: quem chama diz qual versão aceita); a corrida entre esta conferência e o commit é pega pelo
    // @Version no UPDATE (OptimisticLockingFailureException).
    @Transactional
    public Estoque definirDisponivel(UUID produtoId, int quantidade, LongPredicate versaoAceita) {
        var estoque = buscar(produtoId);
        if (!versaoAceita.test(estoque.getVersao())) {
            throw new ConflitoDeVersaoException(estoque.getVersao());
        }
        estoque.definirDisponivel(quantidade);
        return estoque;
    }

    // O ajuste de um item de lote (14.04): sem a versão, porque quem sincroniza (o ERP) manda a quantidade que
    // contou, e ela vale mais que a do Bazar. A corrida com uma reserva ainda é pega pelo @Version.
    @Transactional
    public Estoque ajustarDisponivel(UUID produtoId, int quantidade) {
        var estoque = buscar(produtoId);
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

    // Uma reserva que não pode ser liberada é um bug (o pedido e o estoque divergiram), não um erro do cliente.
    @Transactional
    public void liberar(UUID produtoId, int quantidade) {
        if (estoques.liberar(produtoId, quantidade) == 0) {
            throw new IllegalStateException("Reserva de " + quantidade + " não encontrada no estoque de " + produtoId);
        }
    }
}
