package com.bazar.pedidos.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.bazar.catalogo.dominio.Produto;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;

/**
 * Cria pedidos para os testes, já no status pedido. O pedido chega lá pelas mesmas ações que o código
 * de produção usa (iniciarPagamento, confirmarPagamento...), sem reflexão nem setter de status.
 */
public final class PedidoDeTeste {

    private UUID clienteId = UUID.fromString("0192f3a1-7777-7000-8000-000000000001");
    private final List<Item> itens = new ArrayList<>();
    private StatusPedido status = StatusPedido.CRIADO;

    private record Item(Produto produto, int quantidade) {
    }

    private PedidoDeTeste() {
    }

    public static PedidoDeTeste umPedido() {
        return new PedidoDeTeste();
    }

    public PedidoDeTeste doCliente(UUID clienteId) {
        this.clienteId = clienteId;
        return this;
    }

    public PedidoDeTeste com(Produto produto, int quantidade) {
        itens.add(new Item(produto, quantidade));
        return this;
    }

    public PedidoDeTeste noStatus(StatusPedido status) {
        this.status = status;
        return this;
    }

    public Pedido build() {
        var pedido = new Pedido(clienteId, Instant.parse("2026-10-25T13:30:00Z"));
        if (itens.isEmpty()) {
            pedido.adicionarItem(umProduto().build(), 1);
        }
        itens.forEach(item -> pedido.adicionarItem(item.produto(), item.quantidade()));
        caminhoAte(status).forEach(acao -> acao.accept(pedido));
        return pedido;
    }

    private static List<Consumer<Pedido>> caminhoAte(StatusPedido status) {
        return switch (status) {
            case CRIADO -> List.of();
            case PAGAMENTO_PENDENTE -> List.of(Pedido::iniciarPagamento);
            case PAGO -> List.of(Pedido::iniciarPagamento, Pedido::confirmarPagamento);
            case ENVIADO -> List.of(Pedido::iniciarPagamento, Pedido::confirmarPagamento, Pedido::enviar);
            case ENTREGUE -> List.of(Pedido::iniciarPagamento, Pedido::confirmarPagamento, Pedido::enviar,
                    Pedido::confirmarEntrega);
            case CANCELADO -> List.of(pedido -> pedido.cancelar(MotivoCancelamento.CLIENTE_DESISTIU, null, Instant.parse("2026-10-25T14:00:00Z")));
            case REEMBOLSADO -> List.of(Pedido::iniciarPagamento, Pedido::confirmarPagamento, Pedido::reembolsar);
        };
    }
}
