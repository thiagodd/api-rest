package com.bazar.catalogo.dominio;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.Constraints;
import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.Janelas;
import com.bazar.compartilhado.Keyset;
import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.estoque.dominio.Estoque;
import com.bazar.estoque.dominio.EstoqueRepository;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
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

    /**
     * Uma janela da listagem (13.04). Sem entidades gerenciadas: a projeção seleciona só as colunas de ProdutoResumo.
     * A ordenação já chega com os nomes das propriedades da entidade e com o desempate pelo id (13.02); a posição
     * tem um valor para cada uma dessas propriedades.
     */
    @Transactional(readOnly = true)
    public Window<ProdutoResumo> listar(FiltroDeProdutos filtro, Sort ordenacao, KeysetScrollPosition posicao,
            int limite) {
        var condicao = ProdutoSpecifications.filtradoPor(filtro);
        if (!posicao.isInitial()) {
            condicao = condicao.and(Keyset.alemDe(ordenacao, posicao));
        }
        // Para trás, a busca anda na ordem inversa a partir da posição; o Window volta à ordem da listagem.
        var ordem = posicao.scrollsBackward() ? ordenacao.reverse() : ordenacao;
        var encontrados = produtos.findBy(condicao,
                consulta -> consulta.as(ProdutoResumo.class).sortBy(ordem).limit(limite + 1).all());
        return Janelas.de(encontrados, limite, posicao.scrollsBackward(), produto -> produto.chaves(ordenacao));
    }

    // Para ?embed=categoria (13.05). A categoria sempre existe: a chave estrangeira garante.
    @Transactional(readOnly = true)
    public Categoria categoriaDe(Produto produto) {
        return categorias.findById(produto.getCategoriaId()).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Produto detalhar(UUID id) {
        return produtos.findById(id).orElseThrow(() -> naoEncontrado(id));
    }

    // O produto e o seu estoque (zerado) nascem juntos, ou nenhum dos dois.
    @Transactional
    public Produto criar(Produto produto) {
        exigirCategoria(produto.getCategoriaId());
        // A consulta resolve o caso comum sem uma escrita que falha (e sem o erro no log do banco).
        if (produtos.existsBySku(produto.getSku())) {
            throw new SkuDuplicadoException(produto.getSku());
        }
        // Dois cadastros simultâneos passam os dois pela consulta; quem decide é a constraint unique. O flush aqui
        // faz a violação acontecer dentro do método, e não no commit, onde ela viraria um 500 (14.01).
        try {
            produtos.saveAndFlush(produto);
        } catch (DataIntegrityViolationException ex) {
            if (Constraints.violou(ex, "produto_sku_key")) {
                throw new SkuDuplicadoException(produto.getSku());
            }
            throw ex;
        }
        estoques.save(new Estoque(produto.getId()));
        return produto;
    }

    // Sem save: o produto carregado continua gerenciado até o commit, e o dirty checking gera o UPDATE.
    @Transactional
    public Produto atualizar(UUID id, String nome, @Nullable String descricao, Dinheiro preco, UUID categoriaId,
            Map<String, String> atributos) {
        var produto = produtos.findById(id).orElseThrow(() -> naoEncontrado(id));
        exigirCategoria(categoriaId);
        produto.atualizar(nome, descricao, preco, categoriaId, atributos);
        return produto;
    }

    private static RecursoNaoEncontradoException naoEncontrado(UUID id) {
        return new RecursoNaoEncontradoException("produto-nao-encontrado", id);
    }

    // A chave estrangeira do banco também recusaria, mas com um erro 500 e uma mensagem do PostgreSQL.
    private void exigirCategoria(UUID categoriaId) {
        if (!categorias.existsById(categoriaId)) {
            throw new CampoInvalidoException("categoriaId", "bazar.produto.categoria-inexistente");
        }
    }
}
