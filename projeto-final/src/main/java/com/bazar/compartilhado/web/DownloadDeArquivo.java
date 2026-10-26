package com.bazar.compartilhado.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.jspecify.annotations.Nullable;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ETag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Um arquivo como resposta, com Range (22.02). O Spring já atende o Range de qualquer Resource (206, um trecho ou
 * multipart/byteranges), mas deixa dois buracos, que esta classe fecha:
 *
 * <ul>
 * <li>Não confere o If-Range. Um cliente retomando o download de um arquivo que mudou recebe o resto do arquivo novo
 * colado no começo do antigo. Com um If-Range que não confere, a resposta deve ser o arquivo inteiro (RFC 9110,
 * §13.1.5).</li>
 * <li>Num Range impossível (416), manda o arquivo inteiro no corpo.</li>
 * </ul>
 */
public final class DownloadDeArquivo {

    private DownloadDeArquivo() {
    }

    /**
     * @param resposta o 200 já com os headers do arquivo (Content-Type, Content-Disposition, Cache-Control)
     * @param versao o ETag forte do conteúdo
     */
    public static ResponseEntity<Resource> de(HttpHeaders requisicao, Resource arquivo, ETag versao,
            Instant modificadoEm, ResponseEntity.BodyBuilder resposta) {
        resposta.eTag(versao.formattedTag()).lastModified(modificadoEm);
        if (requisicao.getFirst(HttpHeaders.RANGE) == null) {
            return resposta.body(arquivo);
        }
        try {
            if (!ifRangeConfere(requisicao.getFirst(HttpHeaders.IF_RANGE), versao, modificadoEm)) {
                // O InputStreamResource é o único Resource a que o Spring não aplica o Range: o arquivo inteiro, 200.
                return resposta.header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .contentLength(arquivo.contentLength())
                        .body(new InputStreamResource(arquivo.getInputStream()));
            }
            HttpRange.toResourceRegions(requisicao.getRange(), arquivo);
        }
        catch (IllegalArgumentException ex) {
            // Range fora do arquivo, ou malformado: 416 com o tamanho, e sem corpo.
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + tamanho(arquivo))
                    .build();
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return resposta.body(arquivo);
    }

    // Sem If-Range, o Range vale. Com uma ETag, ela precisa ser idêntica (comparação forte). Com uma data, ela precisa
    // ser exatamente o Last-Modified.
    static boolean ifRangeConfere(@Nullable String ifRange, ETag versao, Instant modificadoEm) {
        if (ifRange == null) {
            return true;
        }
        if (ifRange.startsWith("\"") || ifRange.startsWith("W/")) {
            return ETag.create(ifRange).compare(versao, true);
        }
        try {
            return ZonedDateTime.parse(ifRange, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
                    .equals(modificadoEm.truncatedTo(ChronoUnit.SECONDS));
        }
        catch (DateTimeParseException ex) {
            return false;
        }
    }

    private static long tamanho(Resource arquivo) {
        try {
            return arquivo.contentLength();
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
