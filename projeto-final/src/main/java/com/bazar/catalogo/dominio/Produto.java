package com.bazar.catalogo.dominio;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;
import org.jspecify.annotations.Nullable;

/**
 * Um produto do catálogo. Nasce como RASCUNHO; o id, o status, a versão e as datas
 * são controlados pelo Bazar, nunca pelo cliente.
 */
public class Produto {

    private final UUID id;
    private final String sku;
    private String nome;
    private @Nullable String descricao;
    private Dinheiro preco;
    private UUID categoriaId;
    private StatusProduto status;
    private Map<String, String> atributos;
    private long versao;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    public Produto(String sku, String nome, @Nullable String descricao, Dinheiro preco,
            UUID categoriaId, Map<String, String> atributos) {
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoriaId = categoriaId;
        this.status = StatusProduto.RASCUNHO;
        this.atributos = Map.copyOf(atributos);
        this.versao = 0;
        this.criadoEm = Instant.now();
        this.atualizadoEm = this.criadoEm;
    }

    public void atualizar(String nome, @Nullable String descricao, Dinheiro preco,
            UUID categoriaId, Map<String, String> atributos) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.categoriaId = categoriaId;
        this.atributos = Map.copyOf(atributos);
        this.versao++;
        this.atualizadoEm = Instant.now();
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

    public long getVersao() {
        return versao;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
