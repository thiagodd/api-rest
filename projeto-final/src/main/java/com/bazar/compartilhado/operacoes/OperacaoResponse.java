package com.bazar.compartilhado.operacoes;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import org.springframework.http.ProblemDetail;

/**
 * O estado de uma operação longa (21.02). O resultado (um link) só existe quando ela foi concluída; o erro (um
 * Problem Details), só quando falhou.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OperacaoResponse(UUID id, String tipo, StatusOperacao status, Instant criadaEm, Instant atualizadaEm,
        @Nullable URI resultado, @Nullable ProblemDetail erro) {
}
