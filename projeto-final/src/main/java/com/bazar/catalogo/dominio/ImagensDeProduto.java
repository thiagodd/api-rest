package com.bazar.catalogo.dominio;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ImagensDeProduto {

    private final JdbcClient jdbc;

    public ImagensDeProduto(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    Optional<ImagemProduto> buscar(UUID produtoId, UUID id) {
        return jdbc.sql("select * from imagem_produto where id = :id and produto_id = :produto")
                .param("id", id).param("produto", produtoId)
                .query((linha, n) -> new ImagemProduto(linha.getObject("id", UUID.class),
                        linha.getObject("produto_id", UUID.class), linha.getString("chave"),
                        linha.getString("nome_original"), linha.getString("tipo_conteudo"),
                        linha.getLong("tamanho_bytes"), linha.getInt("largura"), linha.getInt("altura"),
                        linha.getString("texto_alternativo"), linha.getInt("ordem"),
                        linha.getObject("criada_em", OffsetDateTime.class).toInstant()))
                .optional();
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
