package com.bazar.estoque.api;

import java.util.UUID;

import com.bazar.compartilhado.erros.ConflitoDeVersaoException;
import com.bazar.compartilhado.erros.PrecondicaoFalhouException;
import com.bazar.compartilhado.erros.PrecondicaoObrigatoriaException;
import com.bazar.compartilhado.web.Auditavel;
import com.bazar.compartilhado.web.PrecondicaoIfMatch;
import com.bazar.compartilhado.web.VersaoDaRepresentacao;
import com.bazar.estoque.dominio.Estoque;
import com.bazar.estoque.dominio.EstoqueService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.bazar.compartilhado.web.RespostasDeErro.CONFLITO;
import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;
import static com.bazar.compartilhado.web.RespostasDeErro.PRECONDICAO_FALHOU;
import static com.bazar.compartilhado.web.RespostasDeErro.PRECONDICAO_OBRIGATORIA;
import static com.bazar.compartilhado.web.RespostasDeErro.VALIDACAO;

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
    @ApiResponse(responseCode = "200", description = "O saldo, com a versão no ETag",
            headers = @Header(name = "ETag", description = "A versão do estoque; devolva no If-Match do PUT",
                    schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public ResponseEntity<EstoqueResponse> detalhar(@PathVariable UUID produtoId) {
        var estoque = servico.buscar(produtoId);
        return ResponseEntity.ok()
                .eTag(VersaoDaRepresentacao.etag(estoque.getVersao()))
                .body(EstoqueResponse.de(estoque));
    }

    // Concorrência otimista pelo HTTP (20.03): o cliente devolve no If-Match o ETag do GET. Se o estoque mudou
    // desde então, 412; sem nenhuma precondição, 428.
    @PutMapping
    @Auditavel("estoque.definir")
    @ApiResponse(responseCode = "200", description = "O saldo novo, com o ETag novo",
            headers = @Header(name = "ETag", description = "A nova versão do estoque", schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = CONFLITO)
    @ApiResponse(responseCode = "412", ref = PRECONDICAO_FALHOU)
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    @ApiResponse(responseCode = "428", ref = PRECONDICAO_OBRIGATORIA)
    public ResponseEntity<EstoqueResponse> definir(@PathVariable UUID produtoId,
            @Parameter(description = "O ETag lido no GET (comparação forte; * aceita qualquer versão)")
            @RequestHeader(name = HttpHeaders.IF_MATCH, required = false) @Nullable String ifMatch,
            @Valid @RequestBody DefinirEstoqueRequest request) {
        var quantidade = request.quantidadeDisponivel();
        Estoque estoque;
        if (ifMatch != null) {
            var precondicao = PrecondicaoIfMatch.ler(ifMatch);
            try {
                estoque = servico.definirDisponivel(produtoId, quantidade,
                        versao -> precondicao.aceita(VersaoDaRepresentacao.etag(versao)));
            } catch (ConflitoDeVersaoException | OptimisticLockingFailureException e) {
                // Pela conferência no serviço ou pelo @Version no commit: para quem mandou If-Match, é 412.
                throw new PrecondicaoFalhouException();
            }
        } else if (request.versao() != null) {
            // A versão no corpo (10.06) continua valendo, com o 409 de antes, para não quebrar quem já a usa.
            long versaoLida = request.versao();
            estoque = servico.definirDisponivel(produtoId, quantidade, versao -> versao == versaoLida);
        } else {
            throw new PrecondicaoObrigatoriaException();
        }
        return ResponseEntity.ok()
                .eTag(VersaoDaRepresentacao.etag(estoque.getVersao()))
                .body(EstoqueResponse.de(estoque));
    }
}
