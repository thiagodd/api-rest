package com.bazar.clientes.api;

import java.time.Instant;
import java.util.UUID;

import com.bazar.clientes.dominio.Endereco;
import org.jspecify.annotations.Nullable;

/**
 * Sem clienteId: ele já está na URI de onde a representação veio.
 */
public record EnderecoResponse(
        UUID id, String cep, String logradouro, String numero, @Nullable String complemento, String cidade,
        String uf, boolean principal, Instant criadoEm) {

    static EnderecoResponse de(Endereco endereco) {
        return new EnderecoResponse(endereco.id(), endereco.cep(), endereco.logradouro(), endereco.numero(),
                endereco.complemento(), endereco.cidade(), endereco.uf(), endereco.principal(), endereco.criadoEm());
    }
}
