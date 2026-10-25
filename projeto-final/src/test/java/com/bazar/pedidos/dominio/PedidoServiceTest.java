package com.bazar.pedidos.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.estoque.dominio.EstoqueInsuficienteException;
import com.bazar.estoque.dominio.EstoqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

// Um teste unitário do serviço: as dependências são dublês, e nenhum Spring sobe.
// O que depende do banco (o UPDATE da reserva, o rollback) é provado nos testes de integração.
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    PedidoRepository pedidos;

    @Mock
    ProdutoRepository produtos;

    @Mock
    EstoqueService estoques;

    @Mock
    ApplicationEventPublisher eventos;

    // O repositório de clientes ainda guarda tudo num mapa: é um fake pronto, e o teste usa o real.
    ClienteRepository clientes = new ClienteRepository();

    PedidoService service;
    Cliente maria;

    @BeforeEach
    void montarOServico() {
        service = new PedidoService(pedidos, produtos, estoques, clientes, eventos);
        maria = clientes.save(Cliente.novo("Maria Souza", "maria@example.com", "52998224725", null, Canal.WEB));
    }

    @Test
    void criaOPedidoReservandoOEstoqueEPublicandoOEvento() {
        var cleanCode = umProduto().comPreco(12990).build();
        when(produtos.findById(cleanCode.getId())).thenReturn(Optional.of(cleanCode));

        var pedido = service.criar(maria.id(), List.of(new ItemSolicitado(cleanCode.getId(), 2)));

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.getValorTotal()).isEqualTo(Dinheiro.deCentavos(25980, "BRL"));
        verify(estoques).reservar(cleanCode.getId(), 2);
        verify(pedidos).save(pedido);
        verify(eventos).publishEvent(new PedidoCriado(pedido.getId(), maria.id(), Dinheiro.deCentavos(25980, "BRL")));
    }

    @Test
    void reservaCadaProdutoUmaVezEmOrdemDeId() {
        var a = umProduto().comSku("LIV-0001").build();
        var b = umProduto().comSku("LIV-0002").build();
        var menor = a.getId().compareTo(b.getId()) < 0 ? a : b;
        var maior = menor == a ? b : a;
        when(produtos.findById(a.getId())).thenReturn(Optional.of(a));
        when(produtos.findById(b.getId())).thenReturn(Optional.of(b));

        service.criar(maria.id(), List.of(
                new ItemSolicitado(maior.getId(), 1),
                new ItemSolicitado(menor.getId(), 2),
                new ItemSolicitado(maior.getId(), 3)));

        var ordem = inOrder(estoques);
        ordem.verify(estoques).reservar(menor.getId(), 2);
        ordem.verify(estoques).reservar(maior.getId(), 4);
        verifyNoMoreInteractions(estoques);
    }

    @Test
    void recusaUmClienteInexistenteSemTocarNoEstoque() {
        var desconhecido = UUID.fromString("0192f3a1-7777-7000-8000-000000000999");

        assertThatThrownBy(() -> service.criar(desconhecido, List.of(new ItemSolicitado(umProduto().build().getId(), 1))))
                .isInstanceOfSatisfying(CampoInvalidoException.class,
                        e -> assertThat(e.getCampo()).isEqualTo("clienteId"));

        verifyNoInteractions(produtos, estoques, pedidos, eventos);
    }

    @Test
    void apontaOItemCujoProdutoNaoExiste() {
        var cleanCode = umProduto().build();
        Produto inexistente = umProduto().comSku("LIV-0999").build();
        when(produtos.findById(cleanCode.getId())).thenReturn(Optional.of(cleanCode));
        when(produtos.findById(inexistente.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(maria.id(), List.of(
                new ItemSolicitado(cleanCode.getId(), 1),
                new ItemSolicitado(inexistente.getId(), 1))))
                .isInstanceOfSatisfying(CampoInvalidoException.class,
                        e -> assertThat(e.getCampo()).isEqualTo("itens[1].produtoId"));

        verifyNoInteractions(estoques, pedidos, eventos);
    }

    @Test
    void naoGravaNemPublicaQuandoFaltaEstoque() {
        var cleanCode = umProduto().build();
        when(produtos.findById(cleanCode.getId())).thenReturn(Optional.of(cleanCode));
        doThrow(new EstoqueInsuficienteException(cleanCode.getId(), 1, 2)).when(estoques).reservar(cleanCode.getId(), 2);

        assertThatThrownBy(() -> service.criar(maria.id(), List.of(new ItemSolicitado(cleanCode.getId(), 2))))
                .isInstanceOf(EstoqueInsuficienteException.class);

        verify(pedidos, never()).save(any());
        verifyNoInteractions(eventos);
    }
}
