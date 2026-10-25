package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.CategoriaRepository;
import com.bazar.catalogo.dominio.CategoriaService;
import com.bazar.compartilhado.Ids;
import com.bazar.compartilhado.Pagina;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.compartilhado.web.Auditavel;
import com.bazar.compartilhado.web.LinksDePaginacao;
import com.bazar.compartilhado.web.PaginaPedida;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/v1/categorias")
@Tag(name = "categorias", description = "As categorias do catálogo")
public class CategoriaController {

    // A listagem não oferece ?sort=: a ordem é sempre pelo nome, com o desempate pelo id (13.02).
    private static final Sort POR_NOME = Sort.by("nome", "id");

    private final CategoriaRepository repository;
    private final CategoriaService servico;

    public CategoriaController(CategoriaRepository repository, CategoriaService servico) {
        this.repository = repository;
        this.servico = servico;
    }

    // Paginada por offset (13.03): ?page=1&limit=20, com os totais no corpo e first/prev/next/last no header Link.
    @GetMapping
    public ResponseEntity<Pagina<CategoriaResponse>> listar(
            @RequestParam(required = false) @Nullable @Size(min = 2, max = 60) String q,
            @RequestParam(defaultValue = "false") boolean apenasRaizes,
            @Valid @ModelAttribute PaginaPedida pagina) {
        var categorias = servico.listar(q, apenasRaizes, pagina.ordenadaPor(POR_NOME));
        return ResponseEntity.ok()
                .headers(LinksDePaginacao.de(categorias))
                .body(Pagina.de(categorias, CategoriaResponse::de));
    }

    @GetMapping("/{id}")
    public CategoriaResponse detalhar(@PathVariable UUID id) {
        return repository.findById(id)
                .map(CategoriaResponse::de)
                .orElseThrow(() -> naoEncontrada(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CategoriaRequest request) {
        // Ainda é UUID v4; o Bazar passa a gerar UUID v7 em 15.03.
        var categoria = repository.save(request.paraCategoria(Ids.novo()));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(categoria.id())
                .toUri();
        return ResponseEntity.created(location).body(CategoriaResponse.de(categoria));
    }

    @PutMapping("/{id}")
    public CategoriaResponse substituir(@PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
        return CategoriaResponse.de(servico.substituir(id, request.nome(), request.slug(), request.categoriaPaiId()));
    }

    @Auditavel("categoria.remover")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        servico.remover(id);
        return ResponseEntity.noContent().build();
    }

    private static RecursoNaoEncontradoException naoEncontrada(UUID id) {
        return new RecursoNaoEncontradoException(TipoProblema.CATEGORIA_NAO_ENCONTRADA, id);
    }
}
