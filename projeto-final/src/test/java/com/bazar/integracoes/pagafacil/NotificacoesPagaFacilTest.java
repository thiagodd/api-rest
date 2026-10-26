package com.bazar.integracoes.pagafacil;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import com.bazar.LimpezaDoBanco;
import com.bazar.TesteDeIntegracao;
import com.bazar.integracoes.AssinaturaStandardWebhooks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Webhooks de entrada (21.03): a PagaFácil avisando que um pagamento mudou.
 */
@TesteDeIntegracao
class NotificacoesPagaFacilTest {

    static final String CORPO = """
            {"type":"pagamento.aprovado","timestamp":"2026-10-26T12:00:00Z","data":{"pagamentoId":"pf_8812"}}""";

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JdbcClient jdbc;

    @Value("${bazar.pagafacil.segredo-webhook}")
    String segredo;

    @AfterEach
    void limparOBanco() {
        LimpezaDoBanco.limpar(jdbc);
    }

    @Test
    void umaNotificacaoAssinadaEhGravadaUmaVezMesmoSeRepetida() {
        var agora = Instant.now().getEpochSecond();
        assertThat(notificar("msg_1", agora, assinar("msg_1", agora))).hasStatus(HttpStatus.NO_CONTENT);
        // A PagaFácil não recebeu o 204 (timeout) e repete. Mesmo id: 204, sem gravar de novo.
        assertThat(notificar("msg_1", agora, assinar("msg_1", agora))).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(jdbc.sql("select count(*) from notificacao_recebida").query(Long.class).single()).isEqualTo(1);
    }

    @Test
    void umaAssinaturaErradaEh400() {
        var agora = Instant.now().getEpochSecond();
        var deOutroSegredo = new AssinaturaStandardWebhooks(AssinaturaStandardWebhooks.novoSegredo())
                .assinar("msg_1", agora, CORPO.getBytes(StandardCharsets.UTF_8));

        assertThat(notificar("msg_1", agora, deOutroSegredo)).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.codigo").isEqualTo("WEBHOOK_NAO_VERIFICADO");
        assertThat(jdbc.sql("select count(*) from notificacao_recebida").query(Long.class).single()).isZero();
    }

    @Test
    void umaNotificacaoAntigaEh400MesmoComAAssinaturaCerta() {
        var dezMinutosAtras = Instant.now().minusSeconds(600).getEpochSecond();
        assertThat(notificar("msg_1", dezMinutosAtras, assinar("msg_1", dezMinutosAtras)))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    private String assinar(String id, long timestamp) {
        return new AssinaturaStandardWebhooks(segredo).assinar(id, timestamp, CORPO.getBytes(StandardCharsets.UTF_8));
    }

    private MvcTestResult notificar(String id, long timestamp, String assinatura) {
        return mvc.post().uri("/v1/integracoes/pagafacil/notificacoes").contentType(MediaType.APPLICATION_JSON)
                .header("webhook-id", id)
                .header("webhook-timestamp", String.valueOf(timestamp))
                .header("webhook-signature", assinatura)
                .content(CORPO)
                .exchange();
    }
}
