package com.bazar.compartilhado.erros;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

// O catálogo de problemas (16.01) é o contrato dos erros: todo tipo tem título e detalhe nos dois idiomas, e toda
// mensagem de problema do Bazar pertence a um tipo do catálogo.
class CatalogoDeProblemasTest {

    @ParameterizedTest
    @EnumSource(TipoProblema.class)
    void todoTipoTemTituloEDetalheNosDoisIdiomas(TipoProblema tipo) throws IOException {
        for (var arquivo : new String[] {"messages.properties", "messages_pt.properties"}) {
            var mensagens = carregar(arquivo);
            assertThat(mensagens).as(arquivo).containsKeys(
                    "problemDetail.title." + tipo.slug(), "problemDetail." + tipo.slug());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"messages.properties", "messages_pt.properties"})
    void nenhumaMensagemDeProblemaFicaForaDoCatalogo(String arquivo) throws IOException {
        var slugs = Arrays.stream(TipoProblema.values()).map(TipoProblema::slug).collect(Collectors.toSet());

        var orfas = carregar(arquivo).stringPropertyNames().stream()
                .filter(chave -> chave.startsWith("problemDetail.title."))
                .map(chave -> chave.substring("problemDetail.title.".length()))
                // As chaves das exceções do Spring (problemDetail.title.org.springframework...) não são tipos.
                .filter(sufixo -> !sufixo.contains("."))
                .filter(sufixo -> !slugs.contains(sufixo))
                .toList();

        assertThat(orfas).isEmpty();
    }

    private static Properties carregar(String arquivo) throws IOException {
        var mensagens = new Properties();
        try (var entrada = CatalogoDeProblemasTest.class.getClassLoader().getResourceAsStream(arquivo)) {
            mensagens.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        }
        return mensagens;
    }
}
