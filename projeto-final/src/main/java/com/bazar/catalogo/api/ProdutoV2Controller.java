package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;

/**
 * A v2 do produto (18.03), no mesmo caminho da v1. Começa pelo detalhe; as outras operações chegam na 31.09. O
 * serviço é o mesmo da v1: só a representação muda.
 */
@RestController
@RequestMapping(path = "/v{versao}/produtos", version = "2")
@Tag(name = "produtos")
public class ProdutoV2Controller {

    private final ProdutoService servico;
    private final ProdutoV2Mapper mapper;

    public ProdutoV2Controller(ProdutoService servico, ProdutoV2Mapper mapper) {
        this.servico = servico;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    @Operation(operationId = "detalharProdutoV2", summary = "Detalha um produto (v2)")
    @ApiResponse(responseCode = "200", description = "O produto")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public ProdutoV2Response detalhar(@PathVariable UUID id) {
        return mapper.paraResponse(servico.detalhar(id));
    }
}
