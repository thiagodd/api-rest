package com.bazar.catalogo.dominio;

import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.RegraDeNegocioException;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final ProdutoRepository produtos;

    public CategoriaService(CategoriaRepository categorias, ProdutoRepository produtos) {
        this.categorias = categorias;
        this.produtos = produtos;
    }

    @Transactional
    public Categoria substituir(UUID id, String nome, String slug, @Nullable UUID categoriaPaiId) {
        var categoria = buscar(id);
        categoria.substituir(nome, slug, categoriaPaiId);
        return categoria;
    }

    // As duas regras e a remoção na mesma transação.
    @Transactional
    public void remover(UUID id) {
        var categoria = buscar(id);
        var quantidade = produtos.countByCategoriaId(id);
        if (quantidade > 0) {
            throw new RegraDeNegocioException("categoria-com-produtos",
                    Map.of("quantidadeDeProdutos", quantidade), categoria.nome(), quantidade);
        }
        // Sem esta regra, a chave estrangeira de pai_id recusaria a remoção com um erro 500.
        var subcategorias = categorias.countByCategoriaPaiId(id);
        if (subcategorias > 0) {
            throw new RegraDeNegocioException("categoria-com-subcategorias",
                    Map.of("quantidadeDeSubcategorias", subcategorias), categoria.nome(), subcategorias);
        }
        categorias.delete(categoria);
    }

    private Categoria buscar(UUID id) {
        return categorias.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("categoria-nao-encontrada", id));
    }
}
