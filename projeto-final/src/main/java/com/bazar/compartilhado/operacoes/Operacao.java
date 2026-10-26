package com.bazar.compartilhado.operacoes;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/**
 * Uma operação demorada (19.03): um relatório, um reembolso. Os parâmetros ficam em JSON, e cada tipo sabe lê-los.
 */
public record Operacao(UUID id, String tipo, StatusOperacao status, String parametros, int tentativas,
        @Nullable String resultado, @Nullable String erro, Instant criadaEm) {
}
