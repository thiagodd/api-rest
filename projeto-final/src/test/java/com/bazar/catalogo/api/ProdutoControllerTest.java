package com.bazar.catalogo.api;

import java.util.Map;

import com.bazar.catalogo.dominio.ProdutoRepository;
import com.bazar.compartilhado.web.ListaCsvHttpMessageConverter;
import com.bazar.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

// Desde 10.01 os produtos estão no PostgreSQL: o teste sobe a aplicação inteira com um banco num contêiner.
// A Parte 5 (11.03 e 11.04) separa os testes da camada web dos testes com banco.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProdutoControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ProdutoRepository repository;

    @AfterEach
    void limparOBanco() {
        repository.deleteAll();
    }

    @Test
    void criaUmProdutoComoRascunho() {
        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "descricao": "Um guia de boas práticas.",
                          "precoEmCentavos": 12990, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                          "atributos": { "autor": "Robert C. Martin" } }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        assertThat(resposta.getResponse().getHeader("Location")).startsWith("http://localhost/v1/produtos/");
        assertThat(resposta).bodyJson().isLenientlyEqualTo("""
                { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                  "status": "RASCUNHO", "atributos": { "autor": "Robert C. Martin" } }
                """);
        assertThat(resposta).bodyJson().doesNotHavePath("$.versao");
    }

    @Test
    void ignoraOsCamposQueOClienteNaoControla() {
        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "id": "0192f39f-2222-7000-8000-000000000042", "sku": "LIV-0043", "nome": "Refactoring",
                          "precoEmCentavos": 15900, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                          "status": "ATIVO", "versao": 99, "criadoEm": "2020-01-01T00:00:00Z" }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        assertThat(resposta).bodyJson().extractingPath("$.status").isEqualTo("RASCUNHO");
        assertThat(resposta).bodyJson().extractingPath("$.id").isNotEqualTo("0192f39f-2222-7000-8000-000000000042");
        assertThat(resposta).bodyJson().extractingPath("$.criadoEm").asString().doesNotStartWith("2020");
        assertThat(resposta).bodyJson().extractingPath("$.atributos").isEqualTo(Map.of());
    }

    @Test
    void atualizaUmProdutoSemMudarOSku() {
        var location = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "LIV-0044", "nome": "DDD", "precoEmCentavos": 19900,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """)
                .exchange().getResponse().getHeader("Location");
        var caminho = location.substring("http://localhost".length());

        assertThat(mvc.put().uri(caminho)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "nome": "Domain-Driven Design", "descricao": null, "precoEmCentavos": 17990,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003", "atributos": { "autor": "Eric Evans" } }
                        """))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        { "sku": "LIV-0044", "nome": "Domain-Driven Design", "precoEmCentavos": 17990 }
                        """);
    }

    @Test
    void rejeitaUmProdutoInvalidoComOsErrosDeCadaCampo() {
        var resposta = mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "", "nome": "Clean Code", "precoEmCentavos": -5,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """)
                .exchange();

        assertThat(resposta).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(resposta.getMvcResult().getResolvedException()).isInstanceOf(MethodArgumentNotValidException.class);
        var erros = ((MethodArgumentNotValidException) resposta.getMvcResult().getResolvedException()).getFieldErrors();
        assertThat(erros).extracting(FieldError::getField).containsOnly("sku", "precoEmCentavos");
    }

    @Test
    void rejeitaUmProdutoSemPreco() {
        assertThat(mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void listaEmJsonPorPadraoEEmCsvQuandoPedido() {
        mvc.post().uri("/v1/produtos").contentType(MediaType.APPLICATION_JSON).content("""
                { "sku": "LIV-0042", "nome": "Clean Code, 2ª edição", "descricao": "O clássico de \\"Uncle Bob\\"",
                  "precoEmCentavos": 12990, "categoriaId": "0192f3a0-5555-7000-8000-000000000003",
                  "atributos": { "idioma": "pt-BR", "autor": "Robert C. Martin" } }
                """).exchange();

        assertThat(mvc.get().uri("/v1/produtos")).hasContentType(MediaType.APPLICATION_JSON);

        var csv = mvc.get().uri("/v1/produtos").accept(ListaCsvHttpMessageConverter.TEXT_CSV).exchange();
        assertThat(csv).hasStatusOk().hasContentType(ListaCsvHttpMessageConverter.TEXT_CSV);
        assertThat(csv).bodyText()
                .startsWith("id,sku,nome,descricao,precoEmCentavos,categoriaId,status,atributos,criadoEm,atualizadoEm\r\n")
                .contains(",LIV-0042,\"Clean Code, 2ª edição\",\"O clássico de \"\"Uncle Bob\"\"\",12990,")
                .contains(",RASCUNHO,autor=Robert C. Martin; idioma=pt-BR,");
    }

    @Test
    void responde406QuandoPedeCsvDeUmItem() {
        var location = mvc.post().uri("/v1/produtos").contentType(MediaType.APPLICATION_JSON).content("""
                { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                  "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                """).exchange().getResponse().getHeader(HttpHeaders.LOCATION);

        // O CSV só existe para listas: para um item, nenhum conversor escreve text/csv.
        assertThat(mvc.get().uri(location).accept(ListaCsvHttpMessageConverter.TEXT_CSV))
                .hasStatus(HttpStatus.NOT_ACCEPTABLE);
    }

    @Test
    void recusaUmaCategoriaQueNaoExisteCom422() {
        assertThat(mvc.post().uri("/v1/produtos").contentType(MediaType.APPLICATION_JSON).header("Accept-Language", "pt-BR")
                .content("""
                        { "sku": "LIV-0042", "nome": "Clean Code", "precoEmCentavos": 12990,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000999" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/validacao", "codigo": "VALIDACAO",
                          "erros": [ { "campo": "categoriaId", "mensagem": "A categoria informada não existe." } ] }
                        """);
    }

    @Test
    void responde404ComProblemDetails() {
        assertThat(mvc.get().uri("/v1/produtos/0192f39f-2222-7000-8000-000000000999")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.type", v -> assertThat(v).isEqualTo("https://api.bazar.example/problemas/produto-nao-encontrado"))
                .hasPathSatisfying("$.title", v -> assertThat(v).isEqualTo("Produto não encontrado"))
                .hasPathSatisfying("$.detail", v -> assertThat(v)
                        .isEqualTo("Não existe produto com id 0192f39f-2222-7000-8000-000000000999."))
                .hasPathSatisfying("$.instance", v -> assertThat(v).isEqualTo("/v1/produtos/0192f39f-2222-7000-8000-000000000999"))
                .hasPathSatisfying("$.codigo", v -> assertThat(v).isEqualTo("PRODUTO_NAO_ENCONTRADO"))
                .hasPathSatisfying("$.traceId", v -> assertThat(v).asString().matches("[0-9a-f]{32}"));
    }

    @Test
    void responde422ComUmErroPorCampoSemEcoarOValor() {
        assertThat(mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR")
                .content("""
                        { "sku": "liv-42", "nome": "Clean Code", "precoEmCentavos": -5,
                          "categoriaId": "0192f3a0-5555-7000-8000-000000000003" }
                        """))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .isLenientlyEqualTo("""
                        { "type": "https://api.bazar.example/problemas/validacao", "title": "Dados inválidos",
                          "status": 422, "detail": "A requisição tem 2 campos inválidos.", "codigo": "VALIDACAO",
                          "erros": [
                            { "campo": "precoEmCentavos", "mensagem": "deve ser maior que 0" },
                            { "campo": "sku", "mensagem": "deve seguir o formato AAA-0000 (ex.: LIV-0042)" } ] }
                        """);
    }

    @Test
    void responde400ParaUmIdMalformadoSemEcoarOValor() {
        assertThat(mvc.get().uri("/v1/produtos/abc").header(HttpHeaders.ACCEPT_LANGUAGE, "pt-BR"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.detail", v -> assertThat(v).isEqualTo("O valor do parâmetro 'id' é inválido."));
    }

    @Test
    void responde405NoMesmoFormato() {
        assertThat(mvc.patch().uri("/v1/produtos/0192f39f-2222-7000-8000-000000000999"))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson()
                .hasPathSatisfying("$.codigo", v -> assertThat(v).isEqualTo("METHOD_NOT_ALLOWED"))
                .hasPath("$.traceId");
    }

    @Test
    void responde400ParaUmCorpoIlegivel() {
        assertThat(mvc.post().uri("/v1/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"sku\": "))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.codigo", v -> assertThat(v).isEqualTo("REQUISICAO_INVALIDA"));
    }
}
