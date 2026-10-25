package com.bazar.catalogo.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bazar.catalogo.dominio.StatusProduto;
import com.bazar.compartilhado.Dinheiro;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/**
 * Representação v2 do produto (18.03): o preço é só o Dinheiro. O precoEmCentavos, depreciado na v1 (18.01), não
 * existe aqui.
 */
@Schema(name = "ProdutoV2")
public record ProdutoV2Response(
        UUID id,
        String sku,
        String nome,
        @Nullable String descricao,
        Dinheiro preco,
        UUID categoriaId,
        StatusProduto status,
        Map<String, String> atributos,
        Instant criadoEm,
        Instant atualizadoEm) {
}
