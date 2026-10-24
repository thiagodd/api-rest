package com.bazar.clientes.dominio;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * Clientes guardados em memória, enquanto o Bazar não tem banco de dados.
 */
@Repository
public class ClienteRepository {

    private final Map<UUID, Cliente> clientes = new ConcurrentHashMap<>();

    public Optional<Cliente> findById(UUID id) {
        return Optional.ofNullable(clientes.get(id));
    }

    public Cliente save(Cliente cliente) {
        clientes.put(cliente.id(), cliente);
        return cliente;
    }
}
