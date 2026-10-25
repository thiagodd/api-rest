package com.bazar.compartilhado.erros;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

class TratadorGlobalDeErrosTest {

    @RestController
    static class ControllerQueFalha {

        @GetMapping("/falha")
        String falhar() {
            throw new IllegalStateException("senha do banco: s3cr3t");
        }
    }

    // Sem Spring Boot: só o controller e o tratador, num MockMvc standalone.
    private final MockMvcTester mvc = MockMvcTester.of(List.of(new ControllerQueFalha()),
            builder -> builder.setControllerAdvice(new TratadorGlobalDeErros()).build());

    @Test
    void erroInesperadoVira500SemVazarAMensagem() throws Exception {
        var resposta = mvc.get().uri("/falha").exchange();

        assertThat(resposta).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyJson()
                .hasPathSatisfying("$.codigo", v -> assertThat(v).isEqualTo("INTERNAL_SERVER_ERROR"))
                .hasPath("$.traceId");
        assertThat(resposta.getResponse().getContentAsString()).doesNotContain("s3cr3t", "IllegalStateException");
    }
}
