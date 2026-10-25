package com.bazar.compartilhado.erros;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

/**
 * O catálogo de problemas publicado (16.01): a URI de cada type (https://api.bazar.example/problemas/sku-duplicado)
 * abre uma página para pessoas, como a RFC 9457 recomenda. A página é gerada do TipoProblema e das mensagens, então
 * não fica desatualizada, e sai no idioma do Accept-Language (15.05).
 */
@RestController
@RequestMapping(value = "/problemas", produces = MediaType.TEXT_HTML_VALUE)
public class CatalogoDeProblemasController {

    private final MessageSource mensagens;

    public CatalogoDeProblemasController(MessageSource mensagens) {
        this.mensagens = mensagens;
    }

    @GetMapping
    ResponseEntity<String> catalogo() {
        var idioma = LocaleContextHolder.getLocale();
        var html = new StringBuilder(cabecalho(texto("problema.catalogo.titulo", idioma), idioma));
        html.append("<table><tr><th>type</th><th>status</th><th>codigo</th><th>")
                .append(escapar(texto("problema.catalogo.acao", idioma))).append("</th></tr>\n");
        for (var tipo : TipoProblema.values()) {
            html.append("<tr><td><a href=\"/problemas/").append(tipo.slug()).append("\">")
                    .append(escapar(texto("problemDetail.title." + tipo.slug(), idioma))).append("</a></td><td>")
                    .append(tipo.status().value()).append("</td><td><code>").append(tipo.name())
                    .append("</code></td><td>").append(escapar(texto("problema.acao." + tipo.acao(), idioma)))
                    .append("</td></tr>\n");
        }
        return localizada(idioma).body(html.append("</table></body></html>\n").toString());
    }

    @GetMapping("/{slug}")
    ResponseEntity<String> tipo(@PathVariable String slug) {
        var idioma = LocaleContextHolder.getLocale();
        return TipoProblema.doSlug(slug)
                .map(tipo -> localizada(idioma).body(pagina(tipo, idioma)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String pagina(TipoProblema tipo, Locale idioma) {
        return cabecalho(texto("problemDetail.title." + tipo.slug(), idioma), idioma)
                + "<dl>\n"
                + "<dt>type</dt><dd><code>" + tipo.uri() + "</code></dd>\n"
                + "<dt>status</dt><dd>" + tipo.status().value() + " " + tipo.status().getReasonPhrase() + "</dd>\n"
                + "<dt>codigo</dt><dd><code>" + tipo.name() + "</code></dd>\n"
                + "<dt>detail</dt><dd>" + escapar(texto("problemDetail." + tipo.slug(), idioma)) + "</dd>\n"
                + "<dt>" + escapar(texto("problema.catalogo.acao", idioma)) + "</dt><dd>"
                + escapar(texto("problema.acao." + tipo.acao(), idioma)) + "</dd>\n"
                + "</dl>\n<p><a href=\"/problemas\">" + escapar(texto("problema.catalogo.titulo", idioma))
                + "</a></p></body></html>\n";
    }

    // Uma página em um idioma diz qual, e um cache sabe que ela muda com o Accept-Language (15.05).
    private static ResponseEntity.BodyBuilder localizada(Locale idioma) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_LANGUAGE, idioma.toLanguageTag())
                .header(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
    }

    private static String cabecalho(String titulo, Locale idioma) {
        return "<!doctype html>\n<html lang=\"" + idioma.toLanguageTag() + "\"><head><meta charset=\"utf-8\"><title>"
                + escapar(titulo) + "</title></head><body>\n<h1>" + escapar(titulo) + "</h1>\n";
    }

    // Os textos são os mesmos das respostas, com os argumentos ({0}) mostrados como estão.
    private String texto(String chave, Locale idioma) {
        return mensagens.getMessage(chave, null, chave, idioma);
    }

    private static String escapar(String texto) {
        return HtmlUtils.htmlEscape(texto, "UTF-8");
    }
}
