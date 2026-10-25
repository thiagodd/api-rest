package com.bazar.compartilhado.web;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.springdoc.core.utils.SpringDocUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

/**
 * O contrato OpenAPI gerado pelo springdoc (17.02). As anotações dos controllers descrevem cada operação; esta classe
 * cuida do que vale para todas: os dados da API, os tipos que o springdoc não consegue deduzir da classe Java, os
 * erros reutilizáveis e os nomes das operações.
 */
@Configuration
public class ConfiguracaoOpenApi {

    static final String PROBLEMA = "#/components/schemas/Problema";
    static final String DINHEIRO = "#/components/schemas/Dinheiro";

    static {
        SpringDocUtils.getConfig()
                // O Dinheiro tem um serializador próprio (15.02): pela classe, o springdoc descreveria o Currency
                // inteiro e um número, e o JSON real é {"valor": "129.90", "moeda": "BRL"}.
                .replaceWithSchema(Dinheiro.class, new Schema<>().$ref(DINHEIRO))
                // O ProblemDetail guarda as extensões num mapa, que o Jackson espalha no JSON. Pela classe, o contrato
                // teria um campo "properties" que não existe.
                .replaceWithSchema(ProblemDetail.class, new Schema<>().$ref(PROBLEMA))
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
                .description("Problem Details (RFC 9457) com as extensões do Bazar (08.02, 16.01).")
                .required(List.of("type", "title", "status", "codigo", "traceId"))
                .addProperty("type", texto().format("uri"))
                .addProperty("title", texto())
                .addProperty("status", tipo("integer"))
                .addProperty("detail", texto())
                .addProperty("instance", texto().format("uri-reference"))
                .addProperty("codigo", texto()
                        .description("O tipo em MAIÚSCULAS, estável para o código do cliente.")
                        .examples(List.of("PRODUTO_NAO_ENCONTRADO")))
                .addProperty("traceId", texto())
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
