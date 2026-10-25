package com.bazar.compartilhado.erros;

import java.net.URI;
import java.util.Locale;

import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

/**
 * Monta o Problem Details de uma exceção de domínio. É usado pelo tratador global e por quem precisa de um
 * problema sem lançar a exceção: o resultado de cada item de um lote (14.04).
 */
public final class Problemas {

    private Problemas() {
    }

    public static ProblemDetail de(ExcecaoDeDominio ex, @Nullable MessageSource mensagens, Locale idioma) {
        var tipo = ex.getTipo();
        var problema = ErrorResponse.builder(ex, tipo.status(), tipo.slug())
                .type(tipo.uri())
                .titleMessageCode("problemDetail.title." + tipo.slug())
                .detailMessageCode("problemDetail." + tipo.slug())
                .detailMessageArguments(ex.getArgumentos())
                .build()
                .updateAndGetBody(mensagens, idioma);
        ex.getExtensoes().forEach(problema::setProperty);
        problema.setProperty("codigo", tipo.name());
        return problema;
    }
}
