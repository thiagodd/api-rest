package com.bazar.catalogo.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.bazar.catalogo.dominio.ImagemProduto;
import com.bazar.catalogo.dominio.ImagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.bazar.compartilhado.web.RespostasDeErro.CONFLITO;
import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;
import static com.bazar.compartilhado.web.RespostasDeErro.VALIDACAO;

/**
 * Upload das imagens de um produto (22.01), de dois jeitos: multipart, com o arquivo e os metadados juntos, ou o
 * arquivo como corpo da requisição.
 */
@RestController
@Tag(name = "Produtos")
public class ImagemProdutoController {

    /** A parte opcional do multipart com os metadados, em JSON. */
    public record MetadadosDaImagem(
            @Schema(description = "Descrição da imagem para leitores de tela.", example = "Capa do livro Clean Code")
            @Size(max = 300) @Nullable String textoAlternativo) {
    }

    private final ImagemService imagens;

    public ImagemProdutoController(ImagemService imagens) {
        this.imagens = imagens;
    }

    /** Só para o contrato: a forma do multipart. Termina em Request para o contrato tratá-lo como entrada (17.02). */
    record ImagemMultipartRequest(
            @Schema(type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED) String arquivo,
            @Nullable MetadadosDaImagem metadados) {
    }

    // O OpenAPI tem uma operação por caminho e método: as duas formas do upload são uma operação só, com três tipos de
    // corpo. Sem isso, o springdoc juntaria os dois métodos e ficaria com um corpo só.
    @PostMapping(path = "/v1/produtos/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "adicionarImagem", summary = "Adiciona uma imagem ao produto",
            description = "JPEG ou PNG, até 5 MB e 8000 x 8000 pixels, até 10 imagens por produto. O formato é "
                    + "reconhecido pelo conteúdo; o Content-Type declarado é ignorado. Em `multipart/form-data`, a "
                    + "parte `arquivo` e, opcional, a parte `metadados` (JSON). Como corpo binário (`image/jpeg` ou "
                    + "`image/png`), o nome vai em `Content-Disposition: attachment; filename=...`.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = {
                @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                        schema = @Schema(implementation = ImagemMultipartRequest.class)),
                @Content(mediaType = MediaType.IMAGE_JPEG_VALUE, schema = @Schema(type = "string", format = "binary")),
                @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(type = "string", format = "binary"))
            }))
    @ApiResponse(responseCode = "201", description = "A imagem foi gravada.", headers = @Header(name = "Location",
            description = "A URI da imagem", schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    @ApiResponse(responseCode = "409", ref = CONFLITO)
    @ApiResponse(responseCode = "413", description = "O arquivo passa de 5 MB.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "415", description = "O conteúdo não é um JPEG nem um PNG válido.",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", ref = VALIDACAO)
    public ResponseEntity<ImagemProdutoResponse> adicionar(@PathVariable UUID id,
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestPart(name = "metadados", required = false) @Valid @Nullable MetadadosDaImagem metadados)
            throws IOException {
        // getInputStream, e não getBytes: o Tomcat já gravou a parte num arquivo temporário, e ela é copiada para o
        // armazenamento em blocos.
        try (var conteudo = arquivo.getInputStream()) {
            var nome = arquivo.getOriginalFilename();
            return criada(imagens.adicionar(id, nome != null ? nome : "imagem",
                    metadados != null ? metadados.textoAlternativo() : null, conteudo));
        }
    }

    // O arquivo é o corpo, sem multipart: o cliente manda os bytes como estão, e o servidor os lê direto do socket.
    @PostMapping(path = "/v1/produtos/{id}/imagens", consumes = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    @Operation(hidden = true)
    public ResponseEntity<ImagemProdutoResponse> adicionarBinaria(@PathVariable UUID id,
            @RequestHeader(name = HttpHeaders.CONTENT_DISPOSITION, required = false) @Nullable String disposicao,
            InputStream conteudo) {
        var nome = disposicao != null ? ContentDisposition.parse(disposicao).getFilename() : null;
        return criada(imagens.adicionar(id, nome != null ? nome : "imagem", null, conteudo));
    }

    private static ResponseEntity<ImagemProdutoResponse> criada(ImagemProduto imagem) {
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{imagem}")
                .buildAndExpand(imagem.id())
                .toUri();
        return ResponseEntity.created(location).body(ImagemProdutoResponse.de(imagem));
    }
}
