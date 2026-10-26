package com.bazar.compartilhado.operacoes;

import java.time.Duration;
import java.util.UUID;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.relatorios.dominio.RelatorioVendasService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * O ciclo 202 → polling → 303 → resultado (21.02).
 */
@TesteDeIntegracao
class OperacaoControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    FilaDeOperacoes fila;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void o202ApontaAOperacaoQueTerminaNum303ParaORelatorio() {
        var aceito = mvc.post().uri("/v1/relatorios/vendas").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "criadoApos": "2026-10-01T00:00:00Z", "criadoAntes": "2026-11-01T00:00:00Z" }
                        """)
                .exchange();
        assertThat(aceito).hasStatus(HttpStatus.ACCEPTED).hasHeader("Retry-After", "2");
        var operacao = aceito.getResponse().getHeader("Location");
        assertThat(operacao).matches("http://localhost/v1/operacoes/[0-9a-f-]{36}");

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(mvc.get().uri(operacao))
                .hasStatus(HttpStatus.SEE_OTHER));
        var concluida = mvc.get().uri(operacao).exchange();
        var relatorio = concluida.getResponse().getHeader("Location");
        assertThat(relatorio).isEqualTo(operacao.replace("/operacoes/", "/relatorios/"));
        assertThat(concluida).bodyJson().isLenientlyEqualTo("""
                { "tipo": "RELATORIO_VENDAS", "status": "CONCLUIDA" }
                """);

        assertThat(mvc.get().uri(relatorio)).hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        { "periodo": { "de": "2026-10-01T00:00:00Z", "ate": "2026-11-01T00:00:00Z" },
                          "formato": "text/csv" }
                        """);
    }

    @Test
    void emAndamentoEh200ComRetryAfterEORelatorioAindaNaoExiste() {
        var id = fila.enfileirar(RelatorioVendasService.TIPO, """
                { "de": "2026-10-01T00:00:00Z", "ate": "2026-11-01T00:00:00Z" }""");

        assertThat(mvc.get().uri("/v1/operacoes/{id}", id)).hasStatusOk()
                .hasHeader("Retry-After", "2")
                .bodyJson().isLenientlyEqualTo("""
                        { "status": "PENDENTE" }
                        """);
        assertThat(mvc.get().uri("/v1/operacoes/{id}", id)).bodyJson().doesNotHavePath("$.resultado");
        assertThat(mvc.get().uri("/v1/relatorios/{id}", id)).hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson().extractingPath("$.codigo").isEqualTo("RELATORIO_NAO_ENCONTRADO");
    }

    @Test
    void umaOperacaoQueFalhouEh200ComOErroSemAMensagemInterna() throws Exception {
        var id = fila.enfileirar(RelatorioVendasService.TIPO, "{}");
        fila.falhar(id, "PSQLException: relation \"venda_secreta\" does not exist");

        var resposta = mvc.get().uri("/v1/operacoes/{id}", id).exchange();
        assertThat(resposta).hasStatusOk().doesNotContainHeader("Retry-After")
                .bodyJson().isLenientlyEqualTo("""
                        { "status": "FALHOU",
                          "erro": { "type": "https://api.bazar.example/problemas/operacao-falhou",
                                    "status": 500, "codigo": "OPERACAO_FALHOU" } }
                        """);
        assertThat(resposta.getResponse().getContentAsString()).doesNotContain("venda_secreta");
    }

    @Test
    void umaOperacaoInexistenteEh404() {
        assertThat(mvc.get().uri("/v1/operacoes/{id}", UUID.randomUUID())).hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson().extractingPath("$.codigo").isEqualTo("OPERACAO_NAO_ENCONTRADA");
    }
}
