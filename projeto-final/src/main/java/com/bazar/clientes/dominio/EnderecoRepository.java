package com.bazar.clientes.dominio;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

/**
 * Endereços em memória, como os clientes. Não há busca só pelo id do endereço: toda consulta exige o clienteId,
 * e os nomes seguem o Spring Data para a troca por um repositório JPA ser direta.
 */
@Repository
public class EnderecoRepository {

    private final Map<UUID, Endereco> enderecos = new ConcurrentHashMap<>();

    public List<Endereco> findAllByClienteIdOrderByCriadoEm(UUID clienteId) {
        return enderecos.values().stream()
                .filter(endereco -> endereco.clienteId().equals(clienteId))
                .sorted(Comparator.comparing(Endereco::criadoEm).thenComparing(Endereco::id))
                .toList();
    }

    public Optional<Endereco> findByIdAndClienteId(UUID id, UUID clienteId) {
        return Optional.ofNullable(enderecos.get(id)).filter(endereco -> endereco.clienteId().equals(clienteId));
    }

    public Endereco save(Endereco endereco) {
        enderecos.put(endereco.id(), endereco);
        return endereco;
    }

    public void delete(Endereco endereco) {
        enderecos.remove(endereco.id());
    }
}
