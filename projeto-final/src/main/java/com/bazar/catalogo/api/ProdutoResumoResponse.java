package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.StatusProduto;
import com.bazar.compartilhado.web.CamposDaResposta;
import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * Um produto na listagem (10.05). O detalhe completo, com descrição e atributos, fica em GET /v1/produtos/{id}.
 * Com ?fields=, só os campos pedidos saem (13.05).
 */
@JsonFilter(CamposDaResposta.FILTRO)
public record ProdutoResumoResponse(
        UUID id,
        String sku,
        String nome,
        long precoEmCentavos,
        UUID categoriaId,
        StatusProduto status) {
}
