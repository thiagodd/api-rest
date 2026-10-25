package com.bazar.pedidos.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import com.bazar.catalogo.dominio.Produto;
import com.bazar.compartilhado.Dinheiro;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.jspecify.annotations.Nullable;

/**
 * Um pedido de um cliente. Os valores ficam numa única moeda; o frete é zero até a cotação com a FreteJá (24.03).
 */
@Entity
@Table(name = "pedido")
public class Pedido {

    private static final Currency REAL = Currency.getInstance("BRL");

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    // Os itens pertencem ao pedido: são gravados e removidos junto com ele (cascade).
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao")
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(nullable = false, length = 3)
    private Currency moeda;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorItens;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorFrete;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Version
    private @Nullable Long versao;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    protected Pedido() {
    }

    public Pedido(UUID clienteId) {
        // UUID v4 por enquanto; UUID v7 e o número legível (BZR-2026-000123) chegam em 15.03.
        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.status = StatusPedido.CRIADO;
        this.moeda = REAL;
        this.valorItens = BigDecimal.ZERO.setScale(2);
        this.valorFrete = BigDecimal.ZERO.setScale(2);
        this.valorTotal = BigDecimal.ZERO.setScale(2);
        this.criadoEm = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    // O nome, o SKU e o preço são copiados do produto: o pedido registra o que foi comprado, e por quanto,
    // mesmo que o produto mude depois.
    public void adicionarItem(Produto produto, int quantidade) {
        var item = new ItemPedido(this, itens.size() + 1, produto.getId(), produto.getSku(), produto.getNome(),
                quantidade, produto.getPreco().valor());
        itens.add(item);
        valorItens = valorItens.add(item.getSubtotal());
        valorTotal = valorItens.add(valorFrete);
    }

    // Cada ação muda o status só se a transição for permitida (StatusPedido.podeIrPara).
    public void iniciarPagamento() {
        mudarPara(StatusPedido.PAGAMENTO_PENDENTE);
    }

    public void confirmarPagamento() {
        mudarPara(StatusPedido.PAGO);
    }

    public void enviar() {
        mudarPara(StatusPedido.ENVIADO);
    }

    public void confirmarEntrega() {
        mudarPara(StatusPedido.ENTREGUE);
    }

    public void cancelar() {
        mudarPara(StatusPedido.CANCELADO);
    }

    public void reembolsar() {
        mudarPara(StatusPedido.REEMBOLSADO);
    }

    private void mudarPara(StatusPedido destino) {
        if (!status.podeIrPara(destino)) {
            throw new TransicaoInvalidaException(id, status, destino);
        }
        status = destino;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public Dinheiro getValorItens() {
        return new Dinheiro(valorItens, moeda);
    }

    public Dinheiro getValorFrete() {
        return new Dinheiro(valorFrete, moeda);
    }

    public Dinheiro getValorTotal() {
        return new Dinheiro(valorTotal, moeda);
    }

    public Currency getMoeda() {
        return moeda;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
