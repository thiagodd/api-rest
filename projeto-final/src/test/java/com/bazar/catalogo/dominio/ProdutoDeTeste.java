package com.bazar.catalogo.dominio;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;
import org.jspecify.annotations.Nullable;

/**
 * Cria produtos válidos para os testes. Cada teste muda só o que importa para ele:
 * {@code umProduto().comPreco(5000).build()}.
 */
public final class ProdutoDeTeste {

    /** Categoria "Programação", inserida pela migration V3. */
    public static final UUID PROGRAMACAO = UUID.fromString("0192f3a0-5555-7000-8000-000000000003");
    public static final UUID ELETRONICOS = UUID.fromString("0192f3a0-5555-7000-8000-000000000002");

    private String sku = "LIV-0042";
    private String nome = "Clean Code";
    private @Nullable String descricao = null;
    private long precoEmCentavos = 12990;
    private UUID categoriaId = PROGRAMACAO;
    private final Map<String, String> atributos = new HashMap<>();

    private ProdutoDeTeste() {
    }

    public static ProdutoDeTeste umProduto() {
        return new ProdutoDeTeste();
    }

    public ProdutoDeTeste comSku(String sku) {
        this.sku = sku;
        return this;
    }

    public ProdutoDeTeste comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public ProdutoDeTeste comPreco(long centavos) {
        this.precoEmCentavos = centavos;
        return this;
    }

    public ProdutoDeTeste daCategoria(UUID categoriaId) {
        this.categoriaId = categoriaId;
        return this;
    }

    public ProdutoDeTeste comAtributo(String nome, String valor) {
        this.atributos.put(nome, valor);
        return this;
    }

    public Produto build() {
        return new Produto(sku, nome, descricao, Dinheiro.deCentavos(precoEmCentavos, "BRL"), categoriaId, atributos);
    }
}
