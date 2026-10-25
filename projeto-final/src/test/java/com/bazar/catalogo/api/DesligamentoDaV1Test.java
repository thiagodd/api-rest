package com.bazar.catalogo.api;

import java.time.Clock;
import java.time.Instant;

import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.DinheiroMapperImpl;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// O dia seguinte ao sunset (18.04): o ProdutoController (v1) saiu do código, e só o ProdutoV2Controller sobe.
@WebMvcTest(ProdutoV2Controller.class)
@Import({ ProdutoV2MapperImpl.class, DinheiroMapperImpl.class })
class DesligamentoDaV1Test {

    private static final String PRODUTO = "/v1/produtos/01a17566-c2c2-750b-a6fd-e44a3e704746";

    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    ProdutoService produtos;

    @MockitoBean
    Clock relogio;

    @Test
    void depoisDoSunsetAV1Eh410ComOGuiaDeMigracao() {
        when(relogio.instant()).thenReturn(Instant.parse("2027-05-01T12:00:00Z"));

        assertThat(mvc.get().uri(PRODUTO))
                .hasStatus(HttpStatus.GONE)
                .bodyJson().satisfies(json -> {
                    json.assertThat().extractingPath("$.codigo").isEqualTo("VERSAO_DESLIGADA");
                    json.assertThat().extractingPath("$.guiaDeMigracao")
                            .isEqualTo("https://api.bazar.example/docs/migracao/produtos-v2");
                });
    }

    @Test
    void antesDoSunsetTirarOCodigoEh404() {
        // O sunset é uma promessa: tirar a v1 antes da data é um erro (o oasdiff barra no CI), e o cliente recebe o
        // 404 genérico, sem guia de migração.
        when(relogio.instant()).thenReturn(Instant.parse("2027-04-29T12:00:00Z"));

        assertThat(mvc.get().uri(PRODUTO))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson().extractingPath("$.codigo").isEqualTo("NOT_FOUND");
    }
}
