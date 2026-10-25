package com.bazar.clientes.api;

import com.bazar.clientes.dominio.ClienteRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(ClienteController.class)
@Import(ClienteRepository.class)
class ClienteControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void cadastraUmClienteSemDevolverOCpf() {
        assertThat(mvc.post().uri("/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Ana Souza", "email": "ana@example.com", "cpf": "52998224725",
                          "telefone": "+5511987654321" }
                        """))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .hasPathSatisfying("$.nome", nome -> assertThat(nome).isEqualTo("Ana Souza"))
                .doesNotHavePath("$.cpf");
    }

    @Test
    void recusaCpfComDigitoErradoComAMensagemNoIdiomaDoCliente() {
        var resposta = mvc.post().uri("/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                .content("""
                        { "nome": "Ana Souza", "email": "ana@example.com", "cpf": "52998224724" }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        var excecao = (MethodArgumentNotValidException) resposta.getMvcResult().getResolvedException();
        assertThat(excecao.getFieldErrors()).singleElement().satisfies(erro -> {
            assertThat(erro.getField()).isEqualTo("cpf");
            assertThat(erro.getCode()).isEqualTo("Cpf");
            assertThat(erro.getDefaultMessage()).isEqualTo("deve ser um CPF válido (11 dígitos)");
        });
    }

    @Test
    void usaAMensagemPadraoSemAcceptLanguage() {
        var resposta = mvc.post().uri("/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                .content("""
                        { "nome": "Ana Souza", "email": "ana@example.com", "cpf": "52998224724" }
                        """)
                .exchange();

        var excecao = (MethodArgumentNotValidException) resposta.getMvcResult().getResolvedException();
        assertThat(excecao.getFieldErrors()).extracting(FieldError::getDefaultMessage)
                .containsExactly("must be a valid CPF (11 digits)");
    }
}
