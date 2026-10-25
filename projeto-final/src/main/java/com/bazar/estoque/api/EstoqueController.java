package com.bazar.estoque.api;

import java.util.UUID;

import com.bazar.compartilhado.web.Auditavel;
import com.bazar.estoque.dominio.EstoqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// O estoque é um sub-recurso singleton do produto: o desenho desse tipo de rota é assunto de 12.03.
@RestController
@RequestMapping("/v1/produtos/{produtoId}/estoque")
@Tag(name = "estoque", description = "O estoque dos produtos")
public class EstoqueController {

    private final EstoqueService servico;

    public EstoqueController(EstoqueService servico) {
        this.servico = servico;
    }

    @GetMapping
    public EstoqueResponse detalhar(@PathVariable UUID produtoId) {
        return EstoqueResponse.de(servico.buscar(produtoId));
    }

    @PutMapping
    @Auditavel("estoque.definir")
    public EstoqueResponse definir(@PathVariable UUID produtoId, @Valid @RequestBody DefinirEstoqueRequest request) {
        return EstoqueResponse.de(servico.definirDisponivel(produtoId, request.quantidadeDisponivel(), request.versao()));
    }
}
