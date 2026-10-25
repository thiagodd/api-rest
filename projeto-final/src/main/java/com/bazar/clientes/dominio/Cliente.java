package com.bazar.clientes.dominio;

import java.time.Instant;
import java.util.UUID;

import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.Ids;
import org.jspecify.annotations.Nullable;

public record Cliente(
        UUID id, String nome, String email, String cpf, @Nullable String telefone, Canal canalDeCadastro, Instant criadoEm) {

    public static Cliente novo(String nome, String email, String cpf, @Nullable String telefone, Canal canal) {
        return new Cliente(Ids.novo(), nome, email, cpf, telefone, canal, Instant.now());
    }
}
