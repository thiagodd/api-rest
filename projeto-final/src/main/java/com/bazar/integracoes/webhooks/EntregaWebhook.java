package com.bazar.integracoes.webhooks;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Uma entrega de um evento a uma assinatura, com o resultado da última tentativa (21.03). A próxima tentativa só
 * existe enquanto ela está pendente. */
public record EntregaWebhook(UUID id, UUID eventoId, String tipoEvento, String status, int tentativas,
        @Nullable Integer ultimoStatusHttp, @Nullable String ultimoErro, @Nullable Instant proximaTentativaEm, Instant criadaEm,
        @Nullable Instant entregueEm) {
}
