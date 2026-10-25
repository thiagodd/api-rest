package com.bazar.clientes.dominio;

import java.time.Instant;
import java.util.UUID;

import com.bazar.compartilhado.Ids;
import org.jspecify.annotations.Nullable;

/**
 * Um endereço de entrega. Faz parte do agregado Cliente: não existe sem ele, e todo acesso passa pelo clienteId (12.03).
 */
public record Endereco(
        UUID id, UUID clienteId, String cep, String logradouro, String numero, @Nullable String complemento,
        String cidade, String uf, boolean principal, Instant criadoEm) {

    public static Endereco novo(UUID clienteId, Dados dados) {
        return new Endereco(Ids.novo(), clienteId, dados.cep(), dados.logradouro(), dados.numero(),
                dados.complemento(), dados.cidade(), dados.uf(), dados.principal(), Instant.now());
    }

    Endereco com(Dados dados) {
        return new Endereco(id, clienteId, dados.cep(), dados.logradouro(), dados.numero(), dados.complemento(),
                dados.cidade(), dados.uf(), dados.principal(), criadoEm);
    }

    Endereco comPrincipal(boolean principal) {
        return new Endereco(id, clienteId, cep, logradouro, numero, complemento, cidade, uf, principal, criadoEm);
    }

    /** O que o cliente informa ao criar ou substituir um endereço. */
    public record Dados(String cep, String logradouro, String numero, @Nullable String complemento, String cidade,
            String uf, boolean principal) {
    }
}
