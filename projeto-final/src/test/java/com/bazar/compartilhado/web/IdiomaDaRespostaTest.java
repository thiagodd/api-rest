package com.bazar.compartilhado.web;

import java.util.List;

import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TratadorGlobalDeErros;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

// A negociação de idioma das respostas de erro (15.05): o Accept-Language escolhe entre pt-BR e en.
class IdiomaDaRespostaTest {

    @RestController
    static class ControllerDeProdutos {

        @GetMapping("/produtos/1")
        String buscar() {
            throw new RecursoNaoEncontradoException("produto-nao-encontrado", 1);
        }
    }

    private final MockMvcTester mvc = MockMvcTester.of(List.of(new ControllerDeProdutos()), builder -> {
        var mensagens = new ResourceBundleMessageSource();
        mensagens.setBasename("messages");
        mensagens.setDefaultEncoding("UTF-8");
        mensagens.setFallbackToSystemLocale(false);
        var tratador = new TratadorGlobalDeErros();
        tratador.setMessageSource(mensagens);
        return builder.setControllerAdvice(tratador).setLocaleResolver(new ResolvedorDeIdioma()).build();
    });

    @ParameterizedTest(name = "Accept-Language: \"{0}\" → {1}")
    @CsvSource(delimiter = '|', nullValues = "ausente", textBlock = """
            ausente                | pt-BR | Produto não encontrado
            pt-BR                  | pt-BR | Produto não encontrado
            pt-PT                  | pt-BR | Produto não encontrado
            en-US                  | en    | Product not found
            en-GB, pt;q=0.5        | en    | Product not found
            es, pt;q=0.8           | pt-BR | Produto não encontrado
            fr;q=0.9, en;q=0.8     | en    | Product not found
            en;q=0.1, pt-BR;q=0.9  | pt-BR | Produto não encontrado
            fr                     | pt-BR | Produto não encontrado
            *                      | pt-BR | Produto não encontrado
            en_US                  | pt-BR | Produto não encontrado
            """)
    void escolheOIdiomaEDizQualFoiEscolhido(String aceito, String idioma, String titulo) {
        var requisicao = mvc.get().uri("/produtos/1");
        if (aceito != null) {
            requisicao.header(HttpHeaders.ACCEPT_LANGUAGE, aceito);
        }

        assertThat(requisicao.exchange())
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasHeader(HttpHeaders.CONTENT_LANGUAGE, idioma)
                .hasHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE)
                .bodyJson().extractingPath("$.title").isEqualTo(titulo);
    }

    // As exceções do próprio Spring (405, 406, 415...) também têm o detail traduzido, e não só o title.
    @ParameterizedTest(name = "405 em \"{0}\"")
    @CsvSource(delimiter = '|', textBlock = """
            pt-BR | Método não permitido | O método 'DELETE' não é aceito neste recurso.
            en    | Method Not Allowed   | Method 'DELETE' is not supported.
            """)
    void asMensagensDoSpringTambemSaoTraduzidas(String aceito, String titulo, String detalhe) {
        assertThat(mvc.delete().uri("/produtos/1").header(HttpHeaders.ACCEPT_LANGUAGE, aceito))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .bodyJson()
                .hasPathSatisfying("$.title", v -> assertThat(v).isEqualTo(titulo))
                .hasPathSatisfying("$.detail", v -> assertThat(v).isEqualTo(detalhe));
    }
}
