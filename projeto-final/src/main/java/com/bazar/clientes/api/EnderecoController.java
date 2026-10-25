package com.bazar.clientes.api;

import java.util.UUID;

import com.bazar.clientes.dominio.EnderecoService;
import com.bazar.compartilhado.Lista;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Endereços como sub-recurso de composição do cliente (12.03). O clienteId da URI chega a todas as operações
 * do serviço, que só enxerga os endereços daquele cliente.
 */
@RestController
@RequestMapping("/v1/clientes/{clienteId}/enderecos")
@Tag(name = "clientes", description = "Os clientes")
public class EnderecoController {

    private final EnderecoService servico;

    public EnderecoController(EnderecoService servico) {
        this.servico = servico;
    }

    @GetMapping
    public Lista<EnderecoResponse> listar(@PathVariable UUID clienteId) {
        return new Lista<>(servico.listar(clienteId).stream().map(EnderecoResponse::de).toList());
    }

    @PostMapping
    public ResponseEntity<EnderecoResponse> adicionar(@PathVariable UUID clienteId,
            @Valid @RequestBody EnderecoRequest request) {
        var endereco = servico.adicionar(clienteId, request.paraDados());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{enderecoId}")
                .buildAndExpand(endereco.id())
                .toUri();
        return ResponseEntity.created(location).body(EnderecoResponse.de(endereco));
    }

    @GetMapping("/{enderecoId}")
    public EnderecoResponse detalhar(@PathVariable UUID clienteId, @PathVariable UUID enderecoId) {
        return EnderecoResponse.de(servico.detalhar(clienteId, enderecoId));
    }

    @PutMapping("/{enderecoId}")
    public EnderecoResponse substituir(@PathVariable UUID clienteId, @PathVariable UUID enderecoId,
            @Valid @RequestBody EnderecoRequest request) {
        return EnderecoResponse.de(servico.substituir(clienteId, enderecoId, request.paraDados()));
    }

    @DeleteMapping("/{enderecoId}")
    public ResponseEntity<Void> remover(@PathVariable UUID clienteId, @PathVariable UUID enderecoId) {
        servico.remover(clienteId, enderecoId);
        return ResponseEntity.noContent().build();
    }
}
