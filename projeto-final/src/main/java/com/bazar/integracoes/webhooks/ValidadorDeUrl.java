package com.bazar.integracoes.webhooks;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import com.bazar.compartilhado.erros.CampoInvalidoException;

import org.springframework.stereotype.Component;

/**
 * A URL de um webhook é escolhida por quem está de fora, e quem faz a requisição é o Bazar, de dentro da rede dele.
 * Sem validação, um "parceiro" cadastra http://10.0.0.5/admin ou http://169.254.169.254/ (as credenciais da nuvem) e
 * usa o Bazar para alcançar o que ele mesmo não alcança: SSRF (26.05). A validação é feita no cadastro e de novo a
 * cada entrega, porque o DNS pode passar a apontar o mesmo nome para outro endereço.
 */
@Component
class ValidadorDeUrl {

    private final boolean aceitarEnderecosLocais;

    ValidadorDeUrl(ConfiguracaoDeWebhooks configuracao) {
        this.aceitarEnderecosLocais = configuracao.aceitarEnderecosLocais();
    }

    void validar(URI url) {
        var esquema = url.getScheme();
        if (url.getHost() == null || !("https".equals(esquema) || aceitarEnderecosLocais && "http".equals(esquema))) {
            throw new CampoInvalidoException("url", "bazar.webhook.url-sem-https");
        }
        if (aceitarEnderecosLocais) {
            return;
        }
        try {
            for (var endereco : InetAddress.getAllByName(url.getHost())) {
                if (endereco.isLoopbackAddress() || endereco.isSiteLocalAddress() || endereco.isLinkLocalAddress()
                        || endereco.isAnyLocalAddress() || endereco.isMulticastAddress()) {
                    throw new CampoInvalidoException("url", "bazar.webhook.url-interna");
                }
            }
        }
        catch (UnknownHostException ex) {
            throw new CampoInvalidoException("url", "bazar.webhook.url-sem-dns");
        }
    }
}
