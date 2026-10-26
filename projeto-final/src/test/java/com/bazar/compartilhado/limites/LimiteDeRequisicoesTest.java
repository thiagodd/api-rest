package com.bazar.compartilhado.limites;

import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * O rate limiting (23.01) com o Redis do Testcontainers. Cada teste usa um IP só dele, para ter baldes só dele.
 */
@TesteDeIntegracao
class LimiteDeRequisicoesTest {

    @Autowired
    MockMvc mockMvc;

    private static MockHttpServletRequestBuilder doIp(MockHttpServletRequestBuilder requisicao, String ip) {
        return requisicao.with(r -> {
            r.setRemoteAddr(ip);
            return r;
        });
    }

    @Test
    void todaRespostaDizAPoliticaEOQueResta() throws Exception {
        mockMvc.perform(doIp(get("/v1/categorias"), "198.51.100.1"))
                .andExpect(status().isOk())
                .andExpect(header().string("RateLimit-Policy", "\"geral\";q=1000000;w=60"))
                .andExpect(header().string("RateLimit", org.hamcrest.Matchers.startsWith("\"geral\";r=999999;t=")));
    }

    @Test
    void enderecosIpv6DoMesmoBloco64SaoOMesmoCliente() {
        assertThat(LimiteDeRequisicoesFilter.chaveDoEndereco("2001:db8:1:2:aaaa::1"))
                .isEqualTo(LimiteDeRequisicoesFilter.chaveDoEndereco("2001:db8:1:2:bbbb::9"))
                .isEqualTo("2001:db8:1:2:0:0:0:0/64");
        assertThat(LimiteDeRequisicoesFilter.chaveDoEndereco("2001:db8:1:3::1")).isEqualTo("2001:db8:1:3:0:0:0:0/64");
        assertThat(LimiteDeRequisicoesFilter.chaveDoEndereco("203.0.113.9")).isEqualTo("203.0.113.9");
    }

    @Test
    void foraDaApiNaoHaLimite() throws Exception {
        mockMvc.perform(doIp(get("/problemas"), "198.51.100.2"))
                .andExpect(header().doesNotExist("RateLimit"));
    }

    @Test
    void aDecimaPrimeiraExportacaoNaHoraRecebe429() throws Exception {
        // Com Accept: application/xml, nenhum método da exportação serve, e a resposta é um erro sem abrir o cursor; a
        // ficha já foi gasta no filtro.
        var exportacao = get("/v2/produtos/exportacao").accept(MediaType.APPLICATION_XML);
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(doIp(exportacao, "198.51.100.3")).andExpect(status().is4xxClientError())
                    .andExpect(header().string("RateLimit", org.hamcrest.Matchers.containsString("\"exportacao\";r=" + (9 - i))));
        }

        mockMvc.perform(doIp(exportacao, "198.51.100.3").header("Accept-Language", "pt-BR"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("RateLimit-Policy", "\"geral\";q=1000000;w=60, \"exportacao\";q=10;w=3600"))
                .andExpect(header().string("RateLimit",
                        org.hamcrest.Matchers.matchesPattern("\"geral\";r=\\d+;t=1, \"exportacao\";r=0;t=36\\d\\d")))
                .andExpect(header().string("Retry-After", org.hamcrest.Matchers.matchesPattern("3[56]\\d")))
                .andExpect(jsonPath("$.codigo").value("LIMITE_DE_REQUISICOES"))
                .andExpect(jsonPath("$.politica").value("exportacao"))
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.matchesPattern("O limite é de 10 requisições a cada 3.600 segundos. Tente de novo em 3[56]\\d segundos.")));

        // Outro cliente não é afetado.
        mockMvc.perform(doIp(exportacao, "198.51.100.4"))
                .andExpect(header().string("RateLimit", org.hamcrest.Matchers.containsString("\"exportacao\";r=9")));
    }
}
