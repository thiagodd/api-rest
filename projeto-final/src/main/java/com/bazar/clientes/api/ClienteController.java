package com.bazar.clientes.api;

import java.util.UUID;

import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.compartilhado.web.Auditavel;
import com.bazar.compartilhado.web.ContextoRequisicao;
import com.bazar.compartilhado.web.RespostasDeErro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "clientes", description = "Os clientes")
public class ClienteController {

    private final ClienteRepository repository;

    public ClienteController(ClienteRepository repository) {
        this.repository = repository;
    }

    @Auditavel("cliente.cadastrar")
    @PostMapping
    @Operation(operationId = "cadastrarCliente", summary = "Cadastra um cliente")
    @ApiResponse(responseCode = "201", description = "O cliente foi cadastrado", headers = @Header(name = "Location",
            description = "A URI do cliente", schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "422", ref = RespostasDeErro.VALIDACAO)
    public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody CriarClienteRequest request,
            ContextoRequisicao contexto) {
        var cliente = repository.save(request.paraCliente(contexto.canal()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(cliente.id())
                .toUri();
        return ResponseEntity.created(location).body(ClienteResponse.de(cliente));
    }

    @GetMapping("/{id}")
    public ClienteResponse detalhar(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ClienteResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.CLIENTE_NAO_ENCONTRADO, id));
    }
}
