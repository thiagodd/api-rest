package com.bazar.integracoes.webhooks;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.integracoes.AssinaturaStandardWebhooks;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;
import static com.bazar.compartilhado.web.RespostasDeErro.VALIDACAO;

/**
 * As assinaturas de webhook dos parceiros (21.03). "Assinatura" aqui é a inscrição (subscription); a assinatura HMAC
 * de cada entrega é outra coisa.
 */
@RestController
@RequestMapping("/v1/assinaturas-webhook")
@Tag(name = "webhooks", description = "Avisos de eventos para os sistemas dos parceiros")
public class AssinaturaWebhookController {

    static final Set<String> EVENTOS = Set.of(EventosParaWebhooks.PEDIDO_CRIADO);

    private final AssinaturasWebhook assinaturas;
    private final EntregasDeWebhook entregas;
    private final ValidadorDeUrl validador;
    private final Clock relogio;

    AssinaturaWebhookController(AssinaturasWebhook assinaturas, EntregasDeWebhook entregas, ValidadorDeUrl validador,
            Clock relogio) {
        this.assinaturas = assinaturas;
        this.entregas = entregas;
        this.validador = validador;
        this.relogio = relogio;
    }

    public record CriarAssinaturaRequest(@NotBlank @Size(max = 100) String parceiro, @NotNull URI url,
            @NotEmpty List<@NotBlank String> eventos) {
    }

    // O segredo só aparece na resposta da criação: depois, nem o próprio parceiro consegue lê-lo pela API.
    public record AssinaturaResponse(UUID id, String parceiro, URI url, List<String> eventos, boolean ativa,
            Instant criadaEm, @Nullable Instant desativadaEm,
            @Schema(description = "Só na criação. Guarde-o: é com ele que se verifica a assinatura de cada entrega.")
            @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable String segredo) {

        static AssinaturaResponse de(AssinaturaWebhook assinatura, boolean comSegredo) {
            return new AssinaturaResponse(assinatura.id(), assinatura.parceiro(), assinatura.url(),
                    assinatura.eventos(), assinatura.ativa(), assinatura.criadaEm(), assinatura.desativadaEm(),
                    comSegredo ? assinatura.segredo() : null);
        }
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "A assinatura foi criada; o corpo traz o segredo, uma única vez")
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ResponseEntity<AssinaturaResponse> criar(@Valid @RequestBody CriarAssinaturaRequest request) {
        validador.validar(request.url());
        for (int i = 0; i < request.eventos().size(); i++) {
            if (!EVENTOS.contains(request.eventos().get(i))) {
                throw new CampoInvalidoException("eventos[" + i + "]", "bazar.webhook.evento-desconhecido");
            }
        }
        var assinatura = assinaturas.criar(request.parceiro(), request.url(), request.eventos(),
                AssinaturaStandardWebhooks.novoSegredo(), relogio.instant());
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(assinatura.id()).toUri();
        return ResponseEntity.created(location).body(AssinaturaResponse.de(assinatura, true));
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "A assinatura, sem o segredo")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public AssinaturaResponse detalhar(@PathVariable UUID id) {
        return AssinaturaResponse.de(buscar(id), false);
    }

    // O histórico das entregas: o parceiro descobre aqui por que um aviso não chegou, sem abrir um chamado.
    @GetMapping("/{id}/entregas")
    @ApiResponse(responseCode = "200", description = "As 50 entregas mais recentes")
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public List<EntregaWebhook> entregas(@PathVariable UUID id) {
        return entregas.daAssinatura(buscar(id).id(), 50);
    }

    private AssinaturaWebhook buscar(UUID id) {
        return assinaturas.buscar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.ASSINATURA_WEBHOOK_NAO_ENCONTRADA, id));
    }
}
