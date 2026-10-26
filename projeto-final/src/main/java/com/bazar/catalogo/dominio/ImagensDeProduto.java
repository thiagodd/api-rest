package com.bazar.catalogo.dominio;

import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ImagensDeProduto {

    private final JdbcClient jdbc;

    public ImagensDeProduto(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    int proximaOrdem(UUID produtoId) {
        return jdbc.sql("select coalesce(max(ordem), 0) + 1 from imagem_produto where produto_id = :produto")
                .param("produto", produtoId).query(Integer.class).single();
    }

    int quantas(UUID produtoId) {
        return jdbc.sql("select count(*) from imagem_produto where produto_id = :produto")
                .param("produto", produtoId).query(Integer.class).single();
    }

    void inserir(ImagemProduto imagem) {
        jdbc.sql("""
                insert into imagem_produto (id, produto_id, chave, nome_original, tipo_conteudo, tamanho_bytes,
                                            largura, altura, texto_alternativo, ordem, criada_em)
                values (:id, :produto, :chave, :nome, :tipo, :tamanho, :largura, :altura, :alt, :ordem, :criada)""")
                .param("id", imagem.id()).param("produto", imagem.produtoId()).param("chave", imagem.chave())
                .param("nome", imagem.nomeOriginal()).param("tipo", imagem.tipoConteudo())
                .param("tamanho", imagem.tamanhoBytes()).param("largura", imagem.largura())
                .param("altura", imagem.altura()).param("alt", imagem.textoAlternativo())
                .param("ordem", imagem.ordem()).param("criada", imagem.criadaEm().atOffset(ZoneOffset.UTC))
                .update();
    }
}
