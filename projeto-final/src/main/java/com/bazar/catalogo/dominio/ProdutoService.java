package com.bazar.catalogo.dominio;

import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.estoque.dominio.Estoque;
import com.bazar.estoque.dominio.EstoqueRepository;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtos;
    private final CategoriaRepository categorias;
    private final EstoqueRepository estoques;

    public ProdutoService(ProdutoRepository produtos, CategoriaRepository categorias, EstoqueRepository estoques) {
        this.produtos = produtos;
        this.categorias = categorias;
        this.estoques = estoques;
    }

    // O produto e o seu estoque (zerado) nascem juntos, ou nenhum dos dois.
    @Transactional
    public Produto criar(Produto produto) {
        exigirCategoria(produto.getCategoriaId());
        produtos.save(produto);
        estoques.save(new Estoque(produto.getId()));
        return produto;
    }

    // Sem save: o produto carregado continua gerenciado até o commit, e o dirty checking gera o UPDATE.
    @Transactional
    public Produto atualizar(UUID id, String nome, @Nullable String descricao, Dinheiro preco, UUID categoriaId,
            Map<String, String> atributos) {
        var produto = produtos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("produto-nao-encontrado", id));
        exigirCategoria(categoriaId);
        produto.atualizar(nome, descricao, preco, categoriaId, atributos);
        return produto;
    }

    // A chave estrangeira do banco também recusaria, mas com um erro 500 e uma mensagem do PostgreSQL.
    private void exigirCategoria(UUID categoriaId) {
        if (!categorias.existsById(categoriaId)) {
            throw new CampoInvalidoException("categoriaId", "bazar.produto.categoria-inexistente");
        }
    }
}
