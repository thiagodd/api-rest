package com.bazar.integracoes.webhooks;

import java.net.URI;
import java.util.List;

import com.bazar.compartilhado.erros.CampoInvalidoException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidadorDeUrlTest {

    final ValidadorDeUrl emProducao = new ValidadorDeUrl(new ConfiguracaoDeWebhooks(List.of(), false));

    @ParameterizedTest
    @ValueSource(strings = { "http://93.184.215.14/webhooks", "https://127.0.0.1/admin", "https://localhost/x",
            "https://10.0.0.5/", "https://192.168.0.10/", "https://169.254.169.254/latest/meta-data/",
            "https://[::1]/", "ftp://93.184.215.14/" })
    void recusaHttpERedeInterna(String url) {
        assertThatThrownBy(() -> emProducao.validar(URI.create(url))).isInstanceOf(CampoInvalidoException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { "https://93.184.215.14/webhooks" })
    void aceitaHttpsParaUmEnderecoPublico(String url) {
        assertThatCode(() -> emProducao.validar(URI.create(url))).doesNotThrowAnyException();
    }
}
