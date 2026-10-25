package com.bazar.compartilhado.web;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Respostas parciais (13.05): ?fields=id,nome devolve só esses campos de cada item. O tipo do item é marcado com
 * {@code @JsonFilter(CamposDaResposta.FILTRO)}; o controller chama {@link #selecionar}, e o FiltroDeCamposAdvice
 * entrega o filtro ao conversor do Jackson como um hint da escrita.
 */
public final class CamposDaResposta {

    public static final String FILTRO = "campos";

    private static final String ATRIBUTO = CamposDaResposta.class.getName();

    private CamposDaResposta() {
    }

    /**
     * Valida o parâmetro fields contra os componentes do record e guarda o filtro na requisição. Sem fields, não faz
     * nada, e a resposta sai completa.
     */
    public static void selecionar(@Nullable String fields, Class<? extends Record> tipo) {
        var aceitos = Arrays.stream(tipo.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(Collectors.toCollection(TreeSet::new));
        var pedidos = ValoresDeParametro.ler("fields", fields, aceitos);
        if (!pedidos.isEmpty()) {
            RequestContextHolder.currentRequestAttributes().setAttribute(ATRIBUTO,
                    new SimpleFilterProvider().addFilter(FILTRO, SimpleBeanPropertyFilter.filterOutAllExcept(pedidos)),
                    RequestAttributes.SCOPE_REQUEST);
        }
    }

    static @Nullable FilterProvider selecionado() {
        var atributos = RequestContextHolder.getRequestAttributes();
        return atributos == null ? null
                : (FilterProvider) atributos.getAttribute(ATRIBUTO, RequestAttributes.SCOPE_REQUEST);
    }
}
