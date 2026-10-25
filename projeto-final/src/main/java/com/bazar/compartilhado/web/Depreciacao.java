package com.bazar.compartilhado.web;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Uma versão de um recurso em depreciação (18.04): quais caminhos, desde quando, até quando e onde está o guia de
 * migração. A mesma entrada gera os headers das respostas (Deprecation, Sunset, Link), as marcas do contrato
 * (deprecated, x-sunset) e o 410 depois que o código da versão sai.
 *
 * @param versao a versão maior, como no caminho, sem o v ("1")
 * @param caminhos os caminhos do recurso, sem a versão: só eles são depreciados ("/produtos/{id}", mas não
 *                 "/produtos/{produtoId}/estoque", outro recurso)
 */
public record Depreciacao(String versao, List<String> caminhos, LocalDate desde, LocalDate sunset, URI guia) {

    // A política de versões do Bazar: o que significa depreciar e desligar (ADR-0003).
    public static final URI POLITICA = URI.create("https://api.bazar.example/docs/politica-de-versoes");

    // O calendário do Bazar. A v1 do produto foi depreciada no lançamento da v2 (18.03), com seis meses de aviso
    // (BZR-EVO-05).
    public static final List<Depreciacao> CALENDARIO = List.of(
            new Depreciacao("1", List.of("/produtos", "/produtos/buscas", "/produtos/{id}"),
                    LocalDate.of(2026, 10, 26), LocalDate.of(2027, 4, 30),
                    URI.create("https://api.bazar.example/docs/migracao/produtos-v2")));

    public boolean vale(String caminho) {
        var alvo = PathContainer.parsePath(caminho);
        return caminhos.stream()
                .map(padrao -> PathPatternParser.defaultInstance.parse("/v" + versao + padrao))
                .anyMatch(padrao -> padrao.matches(alvo));
    }

    public ZonedDateTime inicio() {
        return desde.atStartOfDay(ZoneOffset.UTC);
    }

    public ZonedDateTime fim() {
        return sunset.atStartOfDay(ZoneOffset.UTC);
    }

    /** A depreciação deste caminho (que já traz a versão: /v1/produtos/...) cujo sunset já passou, se houver. */
    public static Optional<Depreciacao> desligada(String caminho, Instant agora) {
        return CALENDARIO.stream()
                .filter(d -> d.vale(caminho))
                .filter(d -> !agora.isBefore(d.fim().toInstant()))
                .findFirst();
    }
}
