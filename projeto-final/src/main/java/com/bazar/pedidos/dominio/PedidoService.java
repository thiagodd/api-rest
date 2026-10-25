package com.bazar.pedidos.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.estoque.dominio.EstoqueRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;
    private final EstoqueRepository estoques;
    private final ClienteRepository clientes;
    private final ApplicationEventPublisher eventos;

    public PedidoService(PedidoRepository pedidos, ProdutoRepository produtos, EstoqueRepository estoques,
            ClienteRepository clientes, ApplicationEventPublisher eventos) {
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.estoques = estoques;
        this.clientes = clientes;
        this.eventos = eventos;
    }

    // Tudo ou nada: se a reserva de qualquer item falhar, as reservas dos itens anteriores e o pedido são desfeitos.
    @Transactional
    public Pedido criar(UUID clienteId, List<ItemSolicitado> itens) {
        if (clientes.findById(clienteId).isEmpty()) {
            throw new CampoInvalidoException("clienteId", "bazar.pedido.cliente-inexistente");
        }
        var pedido = new Pedido(clienteId);
        for (int i = 0; i < itens.size(); i++) {
            var solicitado = itens.get(i);
            var campo = "itens[" + i + "].produtoId";
            var produto = produtos.findById(solicitado.produtoId())
                    .orElseThrow(() -> new CampoInvalidoException(campo, "bazar.pedido.produto-inexistente"));
            var estoque = estoques.findById(produto.getId())
                    .orElseThrow(() -> new IllegalStateException("Produto sem estoque: " + produto.getId()));
            // Sem save: o estoque está gerenciado pela transação, e o dirty checking grava a mudança no commit.
            estoque.reservar(solicitado.quantidade());
            pedido.adicionarItem(produto, solicitado.quantidade());
        }
        pedidos.save(pedido);
        // Publicado agora, entregue aos ouvintes @TransactionalEventListener só depois do commit.
        eventos.publishEvent(new PedidoCriado(pedido.getId(), clienteId, pedido.getValorTotal()));
        return pedido;
    }

    // Os mais recentes primeiro. A paginação de verdade, com cursor, chega em 13.04.
    @Transactional(readOnly = true)
    public List<Pedido> listarDoCliente(UUID clienteId, int limite) {
        return pedidos.findComItensByClienteIdOrderByCriadoEmDesc(clienteId, Limit.of(limite));
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscar(UUID id) {
        return pedidos.findComItensById(id);
    }
}
