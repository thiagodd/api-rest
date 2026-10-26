package com.bazar.integracoes.webhooks;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Um parceiro que quer ser avisado de certos eventos, numa URL dele (21.03). */
public record AssinaturaWebhook(UUID id, String parceiro, URI url, List<String> eventos, String segredo, boolean ativa,
        Instant criadaEm, @Nullable Instant desativadaEm) {
}
