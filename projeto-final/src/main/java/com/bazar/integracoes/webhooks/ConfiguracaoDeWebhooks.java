package com.bazar.integracoes.webhooks;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * bazar.webhooks.* (21.03).
 *
 * @param esperas quanto esperar antes de cada nova tentativa; com 7 esperas, são 8 tentativas ao todo
 * @param aceitarEnderecosLocais aceitar http e endereços de rede interna (localhost, 10.x, 192.168.x): só em
 *        desenvolvimento e nos testes, nunca em produção (SSRF, 26.05)
 */
@ConfigurationProperties("bazar.webhooks")
public record ConfiguracaoDeWebhooks(List<Duration> esperas, boolean aceitarEnderecosLocais) {
}
