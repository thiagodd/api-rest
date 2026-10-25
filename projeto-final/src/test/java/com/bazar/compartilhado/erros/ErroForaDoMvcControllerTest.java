package com.bazar.compartilhado.erros;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ErroForaDoMvcControllerTest {

    @Test
    void traduzOErroEncaminhadoPeloServidorParaProblemDetails() {
        // Simula o encaminhamento para /error que o Tomcat faz depois de um sendError(401) num filtro.
        var request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 401);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/v1/pedidos");

        var resposta = new ErroForaDoMvcController().erro(request);

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        var problema = resposta.getBody();
        assertThat(problema).isNotNull();
        assertThat(problema.getTitle()).isEqualTo("Unauthorized");
        assertThat(problema.getInstance()).hasToString("/v1/pedidos");
        assertThat(problema.getProperties()).containsEntry("codigo", "UNAUTHORIZED").containsKey("traceId");
    }

    @Test
    void excecaoDeUmFiltroVira500SemVazarAMensagem() {
        var request = new MockHttpServletRequest("GET", "/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException("senha: s3cr3t"));

        var problema = new ErroForaDoMvcController().erro(request).getBody();

        assertThat(problema).isNotNull();
        assertThat(problema.getDetail()).isNull();
        assertThat(problema.getProperties()).containsEntry("codigo", "INTERNAL_SERVER_ERROR");
    }
}
