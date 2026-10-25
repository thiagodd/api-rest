package com.bazar.catalogo.dominio;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

/**
 * Um produto do catálogo. Nasce como RASCUNHO; o id, o status, a versão e as datas
 * são controlados pelo Bazar, nunca pelo cliente.
 */
@Entity
@Table(name = "produto")
public class Produto {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false, length = 8)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 2000)
    private @Nullable String descricao;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "preco_valor", nullable = false, precision = 12, scale = 2))
    @AttributeOverride(name = "moeda", column = @Column(name = "preco_moeda", nullable = false, length = 3))
    private Dinheiro preco;

    @Column(nullable = false)
    private UUID categoriaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusProduto status;

    // Um mapa livre de atributos (autor, voltagem...) numa coluna jsonb do PostgreSQL.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, String> atributos;

    // Nulo até a primeira gravação: é assim que o Spring Data sabe que o produto é novo,
    // mesmo com o id já preenchido. O papel na concorrência fica para 10.06.
    @Version
    private @Nullable Long versao;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(nullable = false)
    private Instant atualizadoEm;

    // Exigido pela JPA, que cria a instância antes de preencher os campos. Não é para o código do Bazar.
    protected Produto() {
    }

    public Produto(String sku, String nome, @Nullable String descricao, Dinheiro preco,
            UUID categoriaId, Map<String, String> atributos) {
        // Ainda é UUID v4; o Bazar passa a gerar UUID v7 em 15.03.
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoriaId = categoriaId;
        this.status = StatusProduto.RASCUNHO;
        this.atributos = Map.copyOf(atributos);
        this.criadoEm = agora();
        this.atualizadoEm = this.criadoEm;
    }

    public void atualizar(String nome, @Nullable String descricao, Dinheiro preco,
            UUID categoriaId, Map<String, String> atributos) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoriaId = categoriaId;
        this.atributos = Map.copyOf(atributos);
        this.atualizadoEm = agora();
    }

    // Soft delete (14.03): o produto sai da loja, mas continua existindo para os pedidos que o citam. Descontinuar
    // de novo não muda nada, nem a data.
    public void descontinuar() {
        if (status != StatusProduto.DESCONTINUADO) {
            status = StatusProduto.DESCONTINUADO;
            atualizadoEm = agora();
        }
    }

    // O PostgreSQL guarda microssegundos; truncar aqui faz a resposta do POST bater com a de um GET posterior.
    private static Instant agora() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public @Nullable String getDescricao() {
        return descricao;
    }

    public Dinheiro getPreco() {
        return preco;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public Map<String, String> getAtributos() {
        return atributos;
    }

    public @Nullable Long getVersao() {
        return versao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
