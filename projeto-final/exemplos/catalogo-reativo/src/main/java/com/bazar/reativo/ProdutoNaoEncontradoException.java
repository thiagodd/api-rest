package com.bazar.reativo;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * O mesmo 404 do Bazar, em Problem Details. O WebFlux tem o mesmo suporte a ErrorResponse que o MVC.
 */
class ProdutoNaoEncontradoException extends ErrorResponseException {

    ProdutoNaoEncontradoException(UUID id) {
        super(HttpStatus.NOT_FOUND, problema(id), null);
    }

    private static ProblemDetail problema(UUID id) {
        var problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Não existe produto com id " + id + ".");
        problema.setType(URI.create("https://api.bazar.example/problemas/produto-nao-encontrado"));
        problema.setTitle("Produto não encontrado");
        problema.setProperty("codigo", "PRODUTO_NAO_ENCONTRADO");
        return problema;
    }
}
