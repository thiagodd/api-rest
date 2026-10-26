package com.bazar.integracoes;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssinaturaStandardWebhooksTest {

    // O vetor de teste da especificação do Standard Webhooks: outra implementação, com o mesmo segredo, a mesma
    // mensagem e o mesmo horário, chega à mesma assinatura.
    static final String SEGREDO = "whsec_MfKQ9r8GKYqrTwjUPD8ILPZIo2LaLaSw";
    static final String ID = "msg_p5jXN8AQM9LWM0D4loKWxJek";
    static final long TIMESTAMP = 1614265330;
    static final byte[] CORPO = "{\"test\": 2432232314}".getBytes(StandardCharsets.UTF_8);
    static final String ASSINATURA = "v1,g0hM9SsE+OTPJTGt/tmIKtSyZlE3uFJELVlNIOLJ1OE=";

    final AssinaturaStandardWebhooks assinatura = new AssinaturaStandardWebhooks(SEGREDO);
    final Instant naHora = Instant.ofEpochSecond(TIMESTAMP);

    @Test
    void assinaComoAEspecificacao() {
        assertThat(assinatura.assinar(ID, TIMESTAMP, CORPO)).isEqualTo(ASSINATURA);
    }

    @Test
    void verificaUmaAssinaturaCertaMesmoNoMeioDeOutras() {
        assertThat(assinatura.verificar(ID, "1614265330", CORPO, "v1,outra= " + ASSINATURA, naHora,
                Duration.ofMinutes(5))).isTrue();
    }

    @Test
    void recusaCorpoAlteradoOuHorarioForaDaTolerancia() {
        var alterado = "{\"test\": 2432232315}".getBytes(StandardCharsets.UTF_8);
        assertThat(assinatura.verificar(ID, "1614265330", alterado, ASSINATURA, naHora, Duration.ofMinutes(5)))
                .isFalse();
        // A assinatura está certa, mas tem 10 minutos: alguém a capturou e está reenviando.
        assertThat(assinatura.verificar(ID, "1614265330", CORPO, ASSINATURA, naHora.plusSeconds(600),
                Duration.ofMinutes(5))).isFalse();
        assertThat(assinatura.verificar(ID, "ontem", CORPO, ASSINATURA, naHora, Duration.ofMinutes(5))).isFalse();
    }
}
