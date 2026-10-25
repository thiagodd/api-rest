package com.bazar.compartilhado.erros;

import java.util.List;

import com.bazar.compartilhado.web.ResolvedorDeIdioma;
import org.junit.jupiter.api.Test;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

// A URI do type leva a uma página para pessoas (16.01).
class CatalogoDeProblemasControllerTest {

    private final MockMvcTester mvc = MockMvcTester.of(List.of(new CatalogoDeProblemasController(mensagens())),
            builder -> builder.setLocaleResolver(new ResolvedorDeIdioma()).build());

    @Test
    void aUriDoTypeAbreAPaginaDoTipo() {
        assertThat(mvc.get().uri("/problemas/conflito-de-versao"))
                .hasStatusOk()
                .hasContentTypeCompatibleWith(MediaType.TEXT_HTML)
                .bodyText()
                .contains("<code>https://api.bazar.example/problemas/conflito-de-versao</code>",
                        "409 Conflict", "<code>CONFLITO_DE_VERSAO</code>",
                        "Buscar o estado atual do recurso, reaplicar a mudança e enviar de novo.");
    }

    @Test
    void oCatalogoListaTodosOsTiposNoIdiomaPedido() {
        var resposta = mvc.get().uri("/problemas").header(HttpHeaders.ACCEPT_LANGUAGE, "en").exchange();

        assertThat(resposta).hasStatusOk().hasHeader(HttpHeaders.CONTENT_LANGUAGE, "en")
                .bodyText().contains("Bazar problem catalog", "<html lang=\"en\">");
        for (var tipo : TipoProblema.values()) {
            assertThat(resposta).bodyText().contains("href=\"/problemas/" + tipo.slug() + "\"");
        }
    }

    @Test
    void umTipoQueNaoExisteEh404() {
        assertThat(mvc.get().uri("/problemas/nao-existe")).hasStatus(HttpStatus.NOT_FOUND);
    }

    private static ResourceBundleMessageSource mensagens() {
        var mensagens = new ResourceBundleMessageSource();
        mensagens.setBasename("messages");
        mensagens.setDefaultEncoding("UTF-8");
        mensagens.setFallbackToSystemLocale(false);
        return mensagens;
    }
}
