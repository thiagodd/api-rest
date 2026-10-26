package com.bazar.pedidos.dominio;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Janelas;
import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.estoque.dominio.EstoqueService;
import org.jspecify.annotations.Nullable;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;
    private final EstoqueService estoques;
    private final ClienteRepository clientes;
    private final ApplicationEventPublisher eventos;
    private final HistoricoDoPedido historico;
    private final Clock relogio;

    public PedidoService(PedidoRepository pedidos, ProdutoRepository produtos, EstoqueService estoques,
            ClienteRepository clientes, ApplicationEventPublisher eventos, HistoricoDoPedido historico,
            Clock relogio) {
        this.pedidos = pedidos;
        this.produtos = produtos;
        this.estoques = estoques;
        this.clientes = clientes;
        this.eventos = eventos;
        this.historico = historico;
        this.relogio = relogio;
    }

    // Tudo ou nada: se a reserva de qualquer item falhar, as reservas dos itens anteriores e o pedido são desfeitos.
    @Transactional
    public Pedido criar(UUID clienteId, List<ItemSolicitado> itens) {
        if (clientes.findById(clienteId).isEmpty()) {
            throw new CampoInvalidoException("clienteId", "bazar.pedido.cliente-inexistente");
        }
        var encontrados = new ArrayList<Produto>();
        for (int i = 0; i < itens.size(); i++) {
            var campo = "itens[" + i + "].produtoId";
            encontrados.add(produtos.findById(itens.get(i).produtoId())
                    .orElseThrow(() -> new CampoInvalidoException(campo, "bazar.pedido.produto-inexistente")));
        }
        // O número só é pedido depois das validações: um pedido recusado não gasta um número. Uma sequence não
        // volta atrás no rollback, então a falta de estoque ainda deixa um buraco na numeração (15.03).
        var agora = relogio.instant();
        var pedido = new Pedido(clienteId, NumeroDoPedido.de(pedidos.proximoNumero(), agora), agora);
        for (int i = 0; i < itens.size(); i++) {
            pedido.adicionarItem(encontrados.get(i), itens.get(i).quantidade());
        }
        reservarEstoque(itens);
        pedidos.save(pedido);
        // O insert do pedido precisa ir antes do histórico, que tem chave estrangeira para ele.
        pedidos.flush();
        // Publicado agora, entregue aos ouvintes @TransactionalEventListener só depois do commit.
        eventos.publishEvent(new PedidoCriado(pedido.getId(), clienteId, pedido.getValorTotal()));
        eventos.publishEvent(historico.registrar(pedido, agora));
        return pedido;
    }

    // Cancela e devolve ao estoque o que estava reservado, numa transação só.
    @Transactional
    public Pedido cancelar(UUID id, MotivoCancelamento motivo, @Nullable String observacao) {
        var pedido = pedidos.findComItensById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.PEDIDO_NAO_ENCONTRADO, id));
        var agora = relogio.instant();
        pedido.cancelar(motivo, observacao, agora);
        // Grava o novo status antes de mexer no estoque. Se outra requisição cancelou este pedido ao mesmo tempo,
        // o UPDATE com "where versao=?" falha aqui (409), e a reserva não é liberada duas vezes.
        pedidos.flush();
        var quantidades = new TreeMap<UUID, Integer>();
        pedido.getItens().forEach(item -> quantidades.merge(item.getProdutoId(), item.getQuantidade(), Integer::sum));
        quantidades.forEach(estoques::liberar);
        eventos.publishEvent(historico.registrar(pedido, agora));
        return pedido;
    }

    // Cada reserva trava a linha do estoque até o commit. Reservando sempre na mesma ordem (por produtoId),
    // dois pedidos com os mesmos produtos esperam um pelo outro em vez de entrar em deadlock (10.06).
    // Itens repetidos do mesmo produto viram uma reserva só.
    private void reservarEstoque(List<ItemSolicitado> itens) {
        var quantidades = new TreeMap<UUID, Integer>();
        itens.forEach(item -> quantidades.merge(item.produtoId(), item.quantidade(), Integer::sum));
        quantidades.forEach(estoques::reservar);
    }

    /**
     * Os pedidos do cliente, dos mais recentes para os mais antigos, uma janela por vez (13.04). A posição tem as
     * chaves criadoEm e id de um pedido já visto; para trás, a janela traz os que vêm antes dele, na mesma ordem.
     * O hasNext da janela diz se há mais pedidos na direção pedida.
     */
    @Transactional(readOnly = true)
    public Window<Pedido> listarDoCliente(UUID clienteId, KeysetScrollPosition posicao, int limite) {
        // Um a mais que o limite: se ele vier, há outra página na mesma direção, sem precisar de um count.
        var umAMais = Limit.of(limite + 1);
        List<Pedido> encontrados;
        if (posicao.isInitial()) {
            encontrados = pedidos.findComItensByClienteIdOrderByCriadoEmDescIdDesc(clienteId, umAMais);
        } else {
            var criadoEm = (Instant) posicao.getKeys().get("criadoEm");
            var id = (UUID) posicao.getKeys().get("id");
            encontrados = posicao.scrollsBackward()
                    ? pedidos.buscarAntesDe(clienteId, criadoEm, id, umAMais)
                    : pedidos.buscarDepoisDe(clienteId, criadoEm, id, umAMais);
        }
        return Janelas.de(encontrados, limite, posicao.scrollsBackward(),
                pedido -> Map.of("criadoEm", pedido.getCriadoEm(), "id", pedido.getId()));
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> buscar(UUID id) {
        return pedidos.findComItensById(id);
    }
}
