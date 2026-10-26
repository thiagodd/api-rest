package com.bazar.compartilhado.web;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.bazar.TesteDeIntegracao;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Os limites de payload (23.02): o tamanho do JSON, contado antes de o Jackson montar qualquer objeto, e as
 * restrições do parser.
 */
@TesteDeIntegracao
class LimitesDePayloadTest {

    @Autowired
    MockMvc mockMvc;

    @LocalServerPort
    int porta;

    @Test
    void umJsonMaiorQueOLimiteEhRecusadoPeloContentLength() throws Exception {
        var lote = "{\"itens\":[" + "{\"produtoId\":\"0192f3a0-5555-7000-8000-000000000001\",\"quantidadeDisponivel\":1},"
                .repeat(15_000) + "]}";

        mockMvc.perform(post("/v1/ajustes-estoque/lotes").contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "pt-BR").content(lote))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.codigo").value("CORPO_GRANDE_DEMAIS"))
                .andExpect(jsonPath("$.detail").value("O corpo da requisição passa do limite de 1 MB."));
    }

    @Test
    void semContentLengthOsBytesSaoContadosDuranteALeitura() throws Exception {
        // Transfer-Encoding: chunked: o filtro não sabe o tamanho de antemão, e interrompe a leitura em 1 MB. São
        // espaços fora das strings, que o parser pula sem limite nenhum: só o contador os pega.
        var nome = "{\"nome\":\"x\"," + " ".repeat(2 * 1024 * 1024) + "\"sku\":\"LIV-0103\"}";
        var corpo = HttpRequest.BodyPublishers.ofInputStream(
                () -> new ByteArrayInputStream(nome.getBytes(StandardCharsets.UTF_8)));

        var resposta = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/v1/produtos"))
                        .header("Content-Type", "application/json")
                        .POST(corpo)
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(resposta.statusCode()).isEqualTo(413);
        assertThat(resposta.body()).contains("\"codigo\":\"CORPO_GRANDE_DEMAIS\"");
    }

    @Test
    void jsonFundoDemaisEhRecusadoPeloParser() throws Exception {
        var fundo = "{\"nome\":\"x\",\"extra\":" + "[".repeat(20) + "]".repeat(20) + "}";

        mockMvc.perform(post("/v1/produtos").contentType(MediaType.APPLICATION_JSON).content(fundo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_FORA_DOS_LIMITES"));
    }

    @Test
    void umaStringLongaDemaisEhRecusadaPeloParser() throws Exception {
        var nome = "{\"nome\":\"" + "a".repeat(200_000) + "\"}";

        mockMvc.perform(post("/v1/produtos").contentType(MediaType.APPLICATION_JSON).content(nome))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_FORA_DOS_LIMITES"));
    }

    @Test
    void numeroLongoDemaisEhRecusadoPeloParser() throws Exception {
        var numero = "{\"nome\":\"x\",\"precoEmCentavos\":" + "1".repeat(40) + "}";

        mockMvc.perform(post("/v1/produtos").contentType(MediaType.APPLICATION_JSON).content(numero))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("JSON_FORA_DOS_LIMITES"));
    }
}
