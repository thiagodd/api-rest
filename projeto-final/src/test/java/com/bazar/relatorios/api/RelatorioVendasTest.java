package com.bazar.relatorios.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.compartilhado.operacoes.FilaDeOperacoes;
import com.bazar.compartilhado.operacoes.Operacao;
import com.bazar.compartilhado.operacoes.StatusOperacao;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@TesteDeIntegracao
class RelatorioVendasTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    FilaDeOperacoes fila;

    @Autowired
    AsyncTaskExecutor applicationTaskExecutor;

    @Autowired
    JdbcClient jdbc;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void aRequisicaoRespondeNaHoraEORelatorioEhGeradoDepois() throws Exception {
        var resposta = mvc.post().uri("/v1/relatorios/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "criadoApos": "2026-10-01T00:00:00Z", "criadoAntes": "2026-11-01T00:00:00Z" }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.ACCEPTED)
                .bodyJson().extractingPath("$.status").isEqualTo("PENDENTE");
        var id = UUID.fromString(JsonPath.read(resposta.getResponse().getContentAsString(), "$.operacaoId"));

        // O @Async gera o arquivo numa virtual thread do executor de tarefas, sem esperar o ProcessadorDeOperacoes.
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(fila.buscar(id)).get()
                .extracting(Operacao::status).isEqualTo(StatusOperacao.CONCLUIDA));
        var arquivo = Path.of(fila.buscar(id).orElseThrow().resultado());
        assertThat(arquivo).exists();
        assertThat(lerPrimeiraLinha(arquivo)).isEqualTo("produtoId,sku,nomeProduto,pedidos,unidades,receita");
    }

    @Test
    void periodoInvertidoEh422() {
        assertThat(mvc.post().uri("/v1/relatorios/vendas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "criadoApos": "2026-11-01T00:00:00Z", "criadoAntes": "2026-10-01T00:00:00Z" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().extractingPath("$.erros[0].campo").isEqualTo("periodo");
    }

    @Test
    void oMdcPassaParaAThreadDoExecutorDeTarefas() throws Exception {
        // spring.task.execution.propagate-context + o Slf4jThreadLocalAccessor da ConfiguracaoDeTarefas.
        MDC.put("operacao", "0192f3a1-9999-7000-8000-000000000123");
        try {
            Callable<String> tarefa = () -> MDC.get("operacao") + " em " + Thread.currentThread();
            var naOutraThread = applicationTaskExecutor.submit(tarefa).get(5, TimeUnit.SECONDS);
            assertThat(naOutraThread).startsWith("0192f3a1-9999-7000-8000-000000000123 em VirtualThread");
        }
        finally {
            MDC.clear();
        }
    }

    private static String lerPrimeiraLinha(Path arquivo) throws Exception {
        try (var linhas = Files.lines(arquivo)) {
            return linhas.findFirst().orElseThrow();
        }
    }
}
