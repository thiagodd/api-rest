package com.bazar.catalogo.dominio;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Os metadados de uma imagem de produto (22.01). O conteúdo está no armazenamento, sob a chave. */
public record ImagemProduto(UUID id, UUID produtoId, String chave, String nomeOriginal, String tipoConteudo,
        long tamanhoBytes, int largura, int altura, @Nullable String textoAlternativo, int ordem, Instant criadaEm) {
}
