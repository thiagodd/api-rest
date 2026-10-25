package com.bazar.catalogo.api;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.bazar.catalogo.dominio.Categoria;
import com.bazar.catalogo.dominio.CategoriaRepository;
import com.bazar.catalogo.dominio.CategoriaService;
import com.bazar.compartilhado.Lista;
import com.bazar.compartilhado.web.ListaCsvHttpMessageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * GET /v1/categorias reescrito em estilo funcional (09.05). O Bazar continua com o controller anotado;
 * esta versão vive só no teste, que mostra o que muda.
 */
@WebMvcTest(CategoriaController.class)
@Import(CategoriaRotasFuncionaisTest.Rotas.class)
class CategoriaRotasFuncionaisTest {

    // O teste é da camada web: as categorias vêm de um repositório simulado, com as três categorias iniciais.
    @MockitoBean
    CategoriaRepository categorias;

    @MockitoBean
    CategoriaService servico;

    @BeforeEach
    void categoriasIniciais() {
        var livros = new Categoria(UUID.fromString("0192f3a0-5555-7000-8000-000000000001"), "Livros", "livros", null);
        var eletronicos = new Categoria(UUID.fromString("0192f3a0-5555-7000-8000-000000000002"), "Eletrônicos", "eletronicos", null);
        var programacao = new Categoria(UUID.fromString("0192f3a0-5555-7000-8000-000000000003"), "Programação", "programacao", livros.id());
        given(categorias.findAllByOrderByNomeAsc()).willReturn(List.of(eletronicos, livros, programacao));
    }

    static final ParameterizedTypeReference<Lista<CategoriaResponse>> LISTA_DE_CATEGORIAS =
            new ParameterizedTypeReference<>() { };

    @TestConfiguration(proxyBeanMethods = false)
    static class Rotas {

        @Bean
        RouterFunction<ServerResponse> rotasDeCategorias(CategoriaRepository repository) {
            var handler = new CategoriaHandler(repository);
            return route()
                    .path("/v1/categorias", rotas -> rotas
                            // A resposta funcional não negocia pelo Accept: o formato é escolhido pela rota.
                            // A primeira rota que casa vence, e sem Accept (*/*) as duas casariam: JSON primeiro.
                            .GET(accept(MediaType.APPLICATION_JSON), handler::listar)
                            .GET(accept(ListaCsvHttpMessageConverter.TEXT_CSV), handler::listarEmCsv))
                    .build();
        }
    }

    static class CategoriaHandler {

        private final CategoriaRepository repository;

        CategoriaHandler(CategoriaRepository repository) {
            this.repository = repository;
        }

        ServerResponse listar(ServerRequest request) {
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(categorias(request), LISTA_DE_CATEGORIAS);
        }

        ServerResponse listarEmCsv(ServerRequest request) {
            return ServerResponse.ok().contentType(ListaCsvHttpMessageConverter.TEXT_CSV)
                    .body(categorias(request), LISTA_DE_CATEGORIAS);
        }

        // Sem @RequestParam, @Size e conversão automática: cada parâmetro é lido e validado à mão.
        private Lista<CategoriaResponse> categorias(ServerRequest request) {
            var q = request.param("q").orElse(null);
            if (q != null && (q.length() < 2 || q.length() > 60)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "q deve ter entre 2 e 60 caracteres");
            }
            var apenasRaizes = Boolean.parseBoolean(request.param("apenasRaizes").orElse("false"));
            return new Lista<>(repository.findAllByOrderByNomeAsc().stream()
                    .filter(c -> q == null || c.nome().toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))
                    .filter(c -> !apenasRaizes || c.categoriaPaiId() == null)
                    .map(CategoriaResponse::de)
                    .toList());
        }
    }

    @Autowired
    MockMvcTester mvc;

    @Test
    void aRotaFuncionalVenceOControllerNoMesmoCaminho() {
        // O RouterFunctionMapping tem ordem -1; o RequestMappingHandlerMapping, 0.
        var resposta = mvc.get().uri("/v1/categorias?apenasRaizes=true").exchange();

        assertThat(resposta.getMvcResult().getHandler()).isNotInstanceOf(org.springframework.web.method.HandlerMethod.class);
        assertThat(resposta).hasStatusOk().hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson().extractingPath("$.itens[*].nome").asArray().containsExactly("Eletrônicos", "Livros");
    }

    @Test
    void escolheOCsvPeloPredicadoDeAccept() {
        assertThat(mvc.get().uri("/v1/categorias?q=livr").accept(ListaCsvHttpMessageConverter.TEXT_CSV))
                .hasStatusOk()
                .hasContentType(ListaCsvHttpMessageConverter.TEXT_CSV)
                .bodyText().isEqualTo("id,nome,slug,categoriaPaiId\r\n0192f3a0-5555-7000-8000-000000000001,Livros,livros,\r\n");
    }

    @Test
    void semAcceptOuComOAcceptDeUmNavegadorRespondeJson() {
        assertThat(mvc.get().uri("/v1/categorias")).hasContentType(MediaType.APPLICATION_JSON);
        assertThat(mvc.get().uri("/v1/categorias")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"))
                .hasContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void aValidacaoManualNaoProduzOFormatoDeErroDoBazar() {
        // O @ControllerAdvice trata a exceção, mas sem o type "validacao" nem a lista de erros do 422 anotado.
        assertThat(mvc.get().uri("/v1/categorias?q=a"))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().isLenientlyEqualTo("""
                        { "status": 422, "detail": "q deve ter entre 2 e 60 caracteres", "codigo": "UNPROCESSABLE_CONTENT" }
                        """);
    }
}
