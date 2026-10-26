package com.bazar.integracoes.webhooks;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.integracoes.AssinaturaStandardWebhooks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Envia as entregas pendentes (21.03): pelo menos uma vez (at-least-once), com retentativas espaçadas e assinatura
 * HMAC. Uma entrega que esgota as tentativas desativa a assinatura: o endpoint do parceiro está fora há horas, e
 * insistir só gasta recursos dos dois lados.
 */
@Component
class EntregadorDeWebhooks {

    private static final Logger log = LoggerFactory.getLogger(EntregadorDeWebhooks.class);

    private static final int LIMITE_POR_RODADA = 50;
    // Tempo que esta instância tem para enviar uma entrega reservada antes que outra possa tentar.
    private static final Duration PRAZO_DA_RESERVA = Duration.ofMinutes(1);

    // Sem seguir redirecionamentos: um 302 para http://169.254.169.254/ passaria por cima da validação da URL.
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    private final EntregasDeWebhook entregas;
    private final AssinaturasWebhook assinaturas;
    private final ValidadorDeUrl validador;
    private final List<Duration> esperas;
    private final Clock relogio;

    EntregadorDeWebhooks(EntregasDeWebhook entregas, AssinaturasWebhook assinaturas, ValidadorDeUrl validador,
            ConfiguracaoDeWebhooks configuracao, Clock relogio) {
        this.entregas = entregas;
        this.assinaturas = assinaturas;
        this.validador = validador;
        this.esperas = configuracao.esperas();
        this.relogio = relogio;
    }

    @Scheduled(fixedDelayString = "${bazar.webhooks.intervalo}")
    void entregarPendentes() {
        var reservadas = entregas.reservar(LIMITE_POR_RODADA, PRAZO_DA_RESERVA, relogio.instant());
        // Em paralelo, uma virtual thread por entrega: um parceiro lento (até 10 s de timeout) não atrasa os outros.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            reservadas.forEach(entrega -> executor.submit(() -> entregar(entrega)));
        }
    }

    void entregar(EntregasDeWebhook.Reservada entrega) {
        Integer statusHttp = null;
        String erro = null;
        try {
            validador.validar(entrega.url());
            var corpo = entrega.corpo().getBytes(StandardCharsets.UTF_8);
            var id = entrega.eventoId().toString();
            // O timestamp é o do envio, não o do evento: o recebedor recusa assinaturas velhas (replay).
            var timestamp = relogio.instant().getEpochSecond();
            var requisicao = HttpRequest.newBuilder(entrega.url())
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Bazar-Webhooks/1.0")
                    .header("webhook-id", id)
                    .header("webhook-timestamp", String.valueOf(timestamp))
                    .header("webhook-signature",
                            new AssinaturaStandardWebhooks(entrega.segredo()).assinar(id, timestamp, corpo))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(corpo))
                    .build();
            statusHttp = http.send(requisicao, HttpResponse.BodyHandlers.discarding()).statusCode();
        }
        catch (IOException | CampoInvalidoException ex) {
            erro = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
        catch (InterruptedException ex) {
            // A aplicação está parando. A reserva vence, e a entrega volta para a fila.
            Thread.currentThread().interrupt();
            return;
        }
        registrar(entrega, statusHttp, erro);
    }

    private void registrar(EntregasDeWebhook.Reservada entrega, @Nullable Integer statusHttp, @Nullable String erro) {
        var agora = relogio.instant();
        if (statusHttp != null && statusHttp >= 200 && statusHttp < 300) {
            entregas.marcarEntregue(entrega.id(), statusHttp, agora);
            return;
        }
        // 410 Gone: o parceiro disse que este endpoint não existe mais. Não adianta insistir.
        if (entrega.tentativas() > esperas.size() || Integer.valueOf(410).equals(statusHttp)) {
            entregas.marcarFalhou(entrega.id(), statusHttp, erro);
            assinaturas.desativar(entrega.assinaturaId(), agora);
            log.warn("Webhook {} desistiu depois de {} tentativas; assinatura {} desativada", entrega.eventoId(),
                    entrega.tentativas(), entrega.assinaturaId());
            return;
        }
        entregas.agendarNovaTentativa(entrega.id(), statusHttp, erro,
                agora.plus(comVariacao(esperas.get(entrega.tentativas() - 1))));
    }

    // Jitter: de 75% a 125% da espera. Sem ele, mil entregas que falharam juntas (o parceiro caiu) voltam todas no
    // mesmo segundo quando ele volta, e o derrubam de novo.
    private static Duration comVariacao(Duration espera) {
        return espera.multipliedBy(ThreadLocalRandom.current().nextInt(75, 126)).dividedBy(100);
    }
}
