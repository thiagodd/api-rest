package com.bazar.catalogo.api;

import java.time.Instant;
import java.util.UUID;

import com.bazar.catalogo.dominio.ImagemProduto;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/** Os metadados de uma imagem. A chave do armazenamento não sai daqui: é um detalhe de onde o arquivo está (22.01). */
public record ImagemProdutoResponse(UUID id,
        @Schema(description = "O nome do arquivo enviado, só para exibição.") String nomeOriginal,
        @Schema(description = "O formato reconhecido pelo conteúdo, não o Content-Type enviado.",
                allowableValues = { "image/jpeg", "image/png" }) String tipoConteudo,
        long tamanhoBytes, int largura, int altura, @Nullable String textoAlternativo, int ordem, Instant criadaEm) {

    static ImagemProdutoResponse de(ImagemProduto imagem) {
        return new ImagemProdutoResponse(imagem.id(), imagem.nomeOriginal(), imagem.tipoConteudo(),
                imagem.tamanhoBytes(), imagem.largura(), imagem.altura(), imagem.textoAlternativo(), imagem.ordem(),
                imagem.criadaEm());
    }
}
