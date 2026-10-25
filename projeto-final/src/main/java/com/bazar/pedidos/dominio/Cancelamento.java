package com.bazar.pedidos.dominio;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.jspecify.annotations.Nullable;

/**
 * Quem cancelou explicou por quê, e quando. Faz parte do pedido (colunas da tabela pedido), não é um recurso à parte.
 */
@Embeddable
public record Cancelamento(
        @Enumerated(EnumType.STRING) @Column(name = "cancelamento_motivo", length = 30) MotivoCancelamento motivo,
        @Column(name = "cancelamento_observacao", length = 500) @Nullable String observacao,
        @Column(name = "cancelado_em") Instant canceladoEm) {
}
