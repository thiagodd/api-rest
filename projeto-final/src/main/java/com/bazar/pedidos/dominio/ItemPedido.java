package com.bazar.pedidos.dominio;

import java.math.BigDecimal;
import java.util.UUID;

import com.bazar.compartilhado.Ids;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Um item do pedido, com uma cópia dos dados do produto no momento da compra.
 */
@Entity
@Table(name = "item_pedido")
public class ItemPedido {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @Column(nullable = false)
    private int posicao;

    @Column(nullable = false)
    private UUID produtoId;

    @Column(nullable = false, length = 8)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nomeProduto;

    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected ItemPedido() {
    }

    ItemPedido(Pedido pedido, int posicao, UUID produtoId, String sku, String nomeProduto, int quantidade,
            BigDecimal precoUnitario) {
        this.id = Ids.novo();
        this.pedido = pedido;
        this.posicao = posicao;
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public String getSku() {
        return sku;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
