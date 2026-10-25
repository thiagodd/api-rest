package com.bazar.compartilhado.web;

import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Os idiomas do Bazar (15.05): português do Brasil, o padrão, e inglês. O Accept-Language escolhe entre os dois,
 * respeitando os pesos (q): "es, pt;q=0.8" é português, e não o primeiro idioma da lista. Um pedido que nenhum dos
 * dois atende ("fr"), "*" ou um cabeçalho inválido recebem o padrão.
 */
public class ResolvedorDeIdioma extends AcceptHeaderLocaleResolver {

    public static final Locale PORTUGUES = Locale.forLanguageTag("pt-BR");
    public static final Locale INGLES = Locale.ENGLISH;

    public ResolvedorDeIdioma() {
        // O "pt" sem país é o que permite casar pt-PT e pt com o português: o Spring só casa pelo idioma com uma
        // entrada sem país. O mesmo vale para "en", que casa en-US e en-GB.
        setSupportedLocales(List.of(PORTUGUES, Locale.of("pt"), INGLES));
        setDefaultLocale(PORTUGUES);
    }

    // Uma resposta só existe em dois idiomas, e o Locale da requisição diz qual: pt-PT recebe o texto em pt-BR, e é
    // isso que o Content-Language deve dizer.
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        if (!pedeAlgumIdioma(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE))) {
            return PORTUGUES;
        }
        var idioma = super.resolveLocale(request);
        return idioma.getLanguage().equals(INGLES.getLanguage()) ? INGLES : PORTUGUES;
    }

    // Com "*" ou um cabeçalho inválido, o servlet devolve o idioma da JVM, e a resposta mudaria de um servidor para
    // outro. Os dois casos recebem o padrão, como uma requisição sem Accept-Language.
    private static boolean pedeAlgumIdioma(@Nullable String cabecalho) {
        if (!StringUtils.hasText(cabecalho)) {
            return false;
        }
        try {
            return Locale.LanguageRange.parse(cabecalho).stream().anyMatch(faixa -> !faixa.getRange().equals("*"));
        } catch (IllegalArgumentException invalido) {
            return false;
        }
    }
}
