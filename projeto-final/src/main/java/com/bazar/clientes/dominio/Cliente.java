package com.bazar.clientes.dominio;

import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record Cliente(UUID id, String nome, String email, String cpf, @Nullable String telefone, Instant criadoEm) {

    public static Cliente novo(String nome, String email, String cpf, @Nullable String telefone) {
        return new Cliente(UUID.randomUUID(), nome, email, cpf, telefone, Instant.now());
    }
}
