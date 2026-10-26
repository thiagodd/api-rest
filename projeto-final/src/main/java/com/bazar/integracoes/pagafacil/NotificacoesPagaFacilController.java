package com.bazar.integracoes.pagafacil;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;

import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.integracoes.AssinaturaStandardWebhooks;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe as notificações da PagaFácil (21.03), no formato Standard Webhooks. Confere a assinatura e o horário, grava a
 * notificação e responde 204 na hora: o processamento (o pedido passa a PAGO) acontece depois, fora da requisição,
 * e chega com o pagamento na Parte 10. Uma repetição (o mesmo webhook-id) também recebe 204, sem ser gravada de novo.
 */
@RestController
@Hidden   // Não faz parte do contrato público: só a PagaFácil chama.
public class NotificacoesPagaFacilController {

    private static final Logger log = LoggerFactory.getLogger(NotificacoesPagaFacilController.class);

    static final String ORIGEM = "pagafacil";
    // Mais que isso de diferença entre o webhook-timestamp e o nosso relógio: uma notificação velha sendo reenviada.
    static final Duration TOLERANCIA = Duration.ofMinutes(5);

    private final AssinaturaStandardWebhooks assinatura;
    private final NotificacoesRecebidas notificacoes;
    private final Clock relogio;

    NotificacoesPagaFacilController(@Value("${bazar.pagafacil.segredo-webhook}") String segredo,
            NotificacoesRecebidas notificacoes, Clock relogio) {
        this.assinatura = new AssinaturaStandardWebhooks(segredo);
        this.notificacoes = notificacoes;
        this.relogio = relogio;
    }

    // O corpo chega como bytes, sem desserializar: a assinatura é calculada sobre os bytes exatos. Um JSON lido e
    // escrito de novo pode mudar espaços ou a ordem dos campos, e a assinatura deixaria de bater.
    @PostMapping("/v1/integracoes/pagafacil/notificacoes")
    public ResponseEntity<Void> receber(@RequestHeader("webhook-id") String id,
            @RequestHeader("webhook-timestamp") String timestamp,
            @RequestHeader("webhook-signature") String assinaturas, @RequestBody byte[] corpo) {
        var agora = relogio.instant();
        if (!assinatura.verificar(id, timestamp, corpo, assinaturas, agora, TOLERANCIA)) {
            throw new RegraDeNegocioException(TipoProblema.WEBHOOK_NAO_VERIFICADO, Map.of());
        }
        if (!notificacoes.registrar(ORIGEM, id, new String(corpo, StandardCharsets.UTF_8), agora)) {
            log.info("Notificação {} da PagaFácil repetida; ignorada", id);
        }
        return ResponseEntity.noContent().build();
    }
}
