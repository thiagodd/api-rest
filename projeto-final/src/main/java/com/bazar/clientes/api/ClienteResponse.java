package com.bazar.clientes.api;

import java.time.Instant;
import java.util.UUID;

import com.bazar.clientes.dominio.Cliente;
import org.jspecify.annotations.Nullable;

/**
 * O CPF não sai na resposta: o cliente já o conhece, e a versão mascarada chega em 26.03.
 */
public record ClienteResponse(UUID id, String nome, String email, @Nullable String telefone, Instant criadoEm) {

    static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.id(), cliente.nome(), cliente.email(), cliente.telefone(), cliente.criadoEm());
    }
}
