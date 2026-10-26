package com.lojista.erp;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * O cliente da API do Bazar dentro do ERP de um lojista (18.05). Não faz parte do Bazar: está aqui para que o teste
 * de consumidor com o Pact rode no mesmo build. Num projeto real, fica no repositório do ERP.
 */
public class CatalogoDoBazar {

    private final RestClient bazar;

    public CatalogoDoBazar(RestClient bazar) {
        this.bazar = bazar;
    }

    public Optional<ProdutoNoErp> produto(UUID id) {
        return bazar.get().uri("/v1/produtos/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange((requisicao, resposta) -> resposta.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))
                        ? Optional.empty()
                        : Optional.ofNullable(resposta.bodyTo(ProdutoNoErp.class)));
    }
}
