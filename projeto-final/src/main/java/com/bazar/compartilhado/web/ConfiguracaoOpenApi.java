package com.bazar.compartilhado.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.bazar.compartilhado.Dinheiro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.utils.SpringDocUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;

/**
 * O contrato OpenAPI gerado pelo springdoc (17.02). As anotações dos controllers descrevem cada operação; esta classe
 * cuida do que vale para todas: os dados da API, os tipos que o springdoc não consegue deduzir da classe Java, os
 * erros reutilizáveis e os nomes das operações.
 */
@Configuration
public class ConfiguracaoOpenApi {

    static final String PROBLEMA = "#/components/schemas/Problema";
    static final String DINHEIRO = "#/components/schemas/Dinheiro";
    static final String LINKS = "#/components/schemas/Links";
    static final String LINK = "#/components/schemas/Link";

    static {
        SpringDocUtils.getConfig()
                // O Dinheiro tem um serializador próprio (15.02): pela classe, o springdoc descreveria o Currency
                // inteiro e um número, e o JSON real é {"valor": "129.90", "moeda": "BRL"}.
                .replaceWithSchema(Dinheiro.class, new Schema<>().$ref(DINHEIRO))
                // O ProblemDetail guarda as extensões num mapa, que o Jackson espalha no JSON. Pela classe, o contrato
                // teria um campo "properties" que não existe.
                .replaceWithSchema(ProblemDetail.class, new Schema<>().$ref(PROBLEMA))
                // No HAL, uma relação é um link ou uma lista de links (curies é sempre uma lista). Pela classe Links,
                // o springdoc só aceitaria um link por relação.
                .replaceWithSchema(Links.class, new Schema<>().$ref(LINKS))
                // Preenchido pelo ContextoRequisicaoResolver a partir de headers, e não um parâmetro de query.
                .addRequestWrapperToIgnore(ContextoRequisicao.class);
    }

    @Bean
    OpenAPI informacoesDaApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API do Bazar")
                        .version("1.0.0")
                        .summary("Catálogo, clientes e pedidos do marketplace Bazar.")
                        .description("""
                                Erros seguem o RFC 9457 (`application/problem+json`). O catálogo de tipos de erro \
                                está em https://api.bazar.example/problemas.""")
                        .contact(new Contact().name("Equipe de plataforma do Bazar")
                                .url("https://api.bazar.example/suporte"))
                        .license(new License().name("Proprietária").url("https://api.bazar.example/termos")))
                // "/" é o servidor que publica o contrato: a Swagger UI local chama a aplicação local.
                .servers(List.of(
                        new Server().url("/").description("Este servidor"),
                        new Server().url("https://api.bazar.example").description("Produção")));
    }

    /**
     * Os erros que as operações referenciam com @ApiResponse(ref = ...), e o 400 em todas as operações: o interceptor
     * de parâmetros recusa um parâmetro de query desconhecido em qualquer rota /v1 (12.02).
     */
    @Bean
    GlobalOpenApiCustomizer errosReutilizaveis() {
        return openApi -> {
            var componentes = openApi.getComponents() == null ? new Components() : openApi.getComponents();
            componentes.addSchemas("Dinheiro", dinheiro());
            componentes.addSchemas("Problema", problema());
            componentes.addResponses("RequisicaoInvalida", erro("A requisição tem a forma ou o tipo errados"));
            componentes.addResponses("NaoEncontrado", erro("O recurso não existe"));
            componentes.addResponses("Conflito", erro("A requisição conflita com o estado atual do recurso"));
            componentes.addResponses("Validacao", erro("Um ou mais valores violam as regras; veja `erros`"));
            openApi.setComponents(componentes);
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(caminho -> caminho.readOperations().forEach(operacao ->
                        operacao.getResponses().putIfAbsent("400",
                                new ApiResponse().$ref("#/components/responses/RequisicaoInvalida"))));
            }
        };
    }

    /**
     * O tipo de um esquema na 3.1 é um conjunto, e a ordem de "null" e do tipo variava com a ordem em que o contexto
     * do Spring era montado: o mesmo código gerava dois contratos. Aqui o "null" vai sempre por último (17.04).
     */
    @Bean
    @SuppressWarnings("rawtypes")   // o swagger-core guarda os esquemas como Schema cru
    GlobalOpenApiCustomizer ordemDosTipos() {
        return openApi -> {
            var esquemas = new ArrayList<Schema>();
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                esquemas.addAll(openApi.getComponents().getSchemas().values());
            }
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(caminho -> caminho.readOperations().forEach(operacao -> {
                    if (operacao.getParameters() != null) {
                        operacao.getParameters().forEach(parametro -> esquemas.add(parametro.getSchema()));
                    }
                }));
            }
            esquemas.forEach(ConfiguracaoOpenApi::nullPorUltimo);
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void nullPorUltimo(@Nullable Schema esquema) {
        if (esquema == null) {
            return;
        }
        if (esquema.getTypes() != null && esquema.getTypes().size() > 1) {
            var ordenados = new LinkedHashSet<String>(new TreeSet<>(esquema.getTypes()));
            if (ordenados.remove("null")) {
                ordenados.add("null");
            }
            esquema.setTypes(ordenados);
        }
        var filhos = new ArrayList<Schema>();
        if (esquema.getProperties() != null) {
            filhos.addAll(esquema.getProperties().values());
        }
        filhos.add(esquema.getItems());
        if (esquema.getAdditionalProperties() instanceof Schema valores) {
            filhos.add(valores);
        }
        for (var composicao : new List[] {esquema.getAllOf(), esquema.getAnyOf(), esquema.getOneOf()}) {
            if (composicao != null) {
                filhos.addAll(composicao);
            }
        }
        filhos.forEach(ConfiguracaoOpenApi::nullPorUltimo);
    }

    /**
     * Substitui o bean de mesmo nome do springdoc, que descreve os links HAL com um link só por relação: o _links de um
     * pedido, com curies em lista, não passaria na validação contra o contrato (17.03).
     */
    @Bean
    GlobalOpenApiCustomizer linksSchemaCustomizer() {
        return openApi -> {
            if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
                openApi.getComponents().addSchemas("Links", links());
                openApi.getComponents().addSchemas("Link", link());
            }
        };
    }

    /**
     * Sem @Operation(operationId = ...), o springdoc usa o nome do método e desempata com sufixos (detalhar_1,
     * detalhar_2), que mudam quando um controller novo aparece. Aqui o nome é o método mais o recurso do controller:
     * detalharCategoria, cancelarPedido.
     */
    @Bean
    OperationCustomizer convencoesDasOperacoes() {
        return (operacao, metodo) -> {
            var anotacao = metodo.getMethodAnnotation(Operation.class);
            if (anotacao == null || anotacao.operationId().isBlank()) {
                var recurso = metodo.getBeanType().getSimpleName().replace("Controller", "");
                operacao.setOperationId(metodo.getMethod().getName() + recurso);
            }
            if (devolveHal(metodo)) {
                aceitarHal(operacao);
            }
            for (var parametro : metodo.getMethodParameters()) {
                if (parametro.getParameterType().equals(ContextoRequisicao.class)) {
                    operacao.addParametersItem(new HeaderParameter()
                            .name(ContextoRequisicaoResolver.CABECALHO_CANAL)
                            .description("O canal do cliente. Ausente ou desconhecido, vale `DESCONHECIDO` (09.02).")
                            .schema(texto().examples(List.of("WEB", "APP", "PARCEIRO"))));
                }
            }
            return operacao;
        };
    }

    // Um EntityModel (12.04) sai como application/hal+json para Accept: */* ou application/hal+json, e como
    // application/json para Accept: application/json. O contrato lista os dois.
    private static boolean devolveHal(HandlerMethod metodo) {
        var tipo = ResolvableType.forMethodReturnType(metodo.getMethod());
        var corpo = ResponseEntity.class.isAssignableFrom(tipo.toClass()) ? tipo.getGeneric(0) : tipo;
        return RepresentationModel.class.isAssignableFrom(corpo.toClass());
    }

    private static void aceitarHal(io.swagger.v3.oas.models.Operation operacao) {
        operacao.getResponses().forEach((status, resposta) -> {
            var conteudo = resposta.getContent();
            if (status.startsWith("2") && conteudo != null && conteudo.containsKey("application/json")) {
                conteudo.addMediaType(MediaTypes.HAL_JSON_VALUE, conteudo.get("application/json"));
            }
        });
    }

    private static Schema<?> links() {
        return tipo("object")
                .description("Os links HAL (12.04): cada relação tem um link, ou uma lista de links.")
                .additionalProperties(new Schema<>().anyOf(List.of(
                        new Schema<>().$ref(LINK),
                        tipo("array").items(new Schema<>().$ref(LINK)))));
    }

    private static Schema<?> link() {
        return tipo("object")
                .required(List.of("href"))
                .addProperty("href", texto())
                .addProperty("templated", tipo("boolean"))
                .addProperty("name", texto())
                .addProperty("title", texto());
    }

    private static Schema<?> dinheiro() {
        return tipo("object")
                .description("Um valor monetário exato, com a moeda (15.02).")
                .required(List.of("valor", "moeda"))
                .addProperty("valor", texto().pattern("^-?\\d+(\\.\\d+)?$")
                        .description("Decimal em texto, com as casas da moeda").examples(List.of("129.90")))
                .addProperty("moeda", texto().pattern("^[A-Z]{3}$")
                        .description("Código ISO 4217").examples(List.of("BRL")));
    }

    private static Schema<?> problema() {
        var erroDeCampo = tipo("object")
                .required(List.of("campo", "mensagem"))
                .properties(Map.of("campo", texto(), "mensagem", texto()));
        return tipo("object")
                .description("""
                        Problem Details (RFC 9457) com as extensões do Bazar (08.02, 16.01). Alguns tipos têm membros \
                        próprios, como `quantidadeDisponivel` no estoque insuficiente: veja o catálogo em /problemas.""")
                .additionalProperties(true)
                .required(List.of("type", "title", "status", "codigo", "traceId"))
                .addProperty("type", texto().format("uri"))
                .addProperty("title", texto())
                .addProperty("status", tipo("integer"))
                .addProperty("detail", texto())
                .addProperty("instance", texto().format("uri-reference"))
                .addProperty("codigo", texto()
                        .description("O tipo em MAIÚSCULAS, estável para o código do cliente.")
                        .examples(List.of("PRODUTO_NAO_ENCONTRADO")))
                .addProperty("traceId", texto().pattern("^[0-9a-f]{32}$")
                        .description("No formato do trace-id do W3C Trace Context. Liga o erro ao log, para o suporte."))
                .addProperty("erros", tipo("array").items(erroDeCampo)
                        .description("Só nos erros de validação (422): um item por campo."));
    }

    private static ApiResponse erro(String descricao) {
        return new ApiResponse()
                .description(descricao)
                .content(new Content().addMediaType("application/problem+json",
                        new MediaType().schema(new Schema<>().$ref(PROBLEMA))));
    }

    private static Schema<Object> texto() {
        return tipo("string");
    }

    // Na OpenAPI 3.1, o tipo é uma lista (type: [string, "null"]): o swagger-core serializa types, e não type.
    private static Schema<Object> tipo(String tipo) {
        return new Schema<>().types(Set.of(tipo));
    }
}
