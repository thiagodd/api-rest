package com.bazar.clientes.api;

import java.util.UUID;

import com.bazar.clientes.dominio.ClienteRepository;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/clientes")
public class ClienteController {

    private final ClienteRepository repository;

    public ClienteController(ClienteRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody CriarClienteRequest request) {
        var cliente = repository.save(request.paraCliente());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cliente.id())
                .toUri();
        return ResponseEntity.created(location).body(ClienteResponse.de(cliente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> detalhar(@PathVariable UUID id) {
        return ResponseEntity.of(repository.findById(id).map(ClienteResponse::de));
    }
}
