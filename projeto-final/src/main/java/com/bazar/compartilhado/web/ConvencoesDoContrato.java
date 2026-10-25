package com.bazar.compartilhado.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.validation.Constraint;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Component;

/**
 * Duas convenções do Bazar que o springdoc não deduz das classes (17.02):
 *
 * <ul>
 * <li>Enums são listas extensíveis (15.04): o contrato lista os valores em examples, e não num enum fechado, que
 * viraria um enum fechado nos clientes gerados.</li>
 * <li>As respostas trazem todos os campos, com null explícito (02.06): todo campo de uma resposta é required, menos
 * os omitidos de propósito com @JsonInclude(NON_NULL). Nas entradas, quem diz o que é obrigatório é a validação
 * (@NotNull, @NotBlank), que o springdoc já lê.</li>
 * </ul>
 */
@Component
public class ConvencoesDoContrato implements ModelConverter {

    @Override
    public @Nullable Schema resolve(AnnotatedType tipo, ModelConverterContext contexto,
            Iterator<ModelConverter> cadeia) {
        if (!cadeia.hasNext()) {
            return null;
        }
        Schema esquema = cadeia.next().resolve(tipo, contexto, cadeia);
        if (esquema == null) {
            return null;
        }
        Class<?> classe = Json.mapper().constructType(tipo.getType()).getRawClass();
        if (classe.isEnum() && esquema.getEnum() != null) {
            tornarExtensivel(esquema);
        }
        else if (classe.isRecord() && classe.getPackageName().startsWith("com.bazar") && !ehEntrada(classe)) {
            var modelo = esquema.get$ref() == null ? esquema : contexto.getDefinedModels().get(nome(esquema));
            if (modelo != null) {
                modelo.setRequired(camposSemprePresentes(classe));
            }
        }
        return esquema;
    }

    @SuppressWarnings("unchecked")
    private static void tornarExtensivel(Schema esquema) {
        esquema.setExamples(List.copyOf(esquema.getEnum()));
        esquema.setEnum(null);
        var aviso = "Lista extensível: valores novos podem ser acrescentados, e o cliente deve tolerar um valor que não "
                + "conhece (15.04).";
        esquema.setDescription(esquema.getDescription() == null ? aviso : esquema.getDescription() + " " + aviso);
    }

    // Uma entrada é um *Request (ou um tipo aninhado num) ou um record validado: respostas não levam validação.
    private static boolean ehEntrada(Class<?> classe) {
        for (var atual = classe; atual != null; atual = atual.getEnclosingClass()) {
            if (atual.getSimpleName().endsWith("Request")) {
                return true;
            }
        }
        // As anotações de validação de um componente chegam também ao método de acesso (todas aceitam METHOD).
        return Arrays.stream(classe.getRecordComponents())
                .flatMap(componente -> Arrays.stream(componente.getAccessor().getAnnotations()))
                .anyMatch(ConvencoesDoContrato::ehRestricao);
    }

    private static boolean ehRestricao(Annotation anotacao) {
        return anotacao.annotationType().isAnnotationPresent(Constraint.class);
    }

    private static List<String> camposSemprePresentes(Class<?> classe) {
        var omiteNulosNaClasse = omiteNulos(classe.getAnnotation(JsonInclude.class));
        return Arrays.stream(classe.getRecordComponents())
                .filter(componente -> !(anotadoComNullable(componente)
                        && (omiteNulosNaClasse || omiteNulos(componente.getAccessor().getAnnotation(JsonInclude.class)))))
                .map(RecordComponent::getName)
                .toList();
    }

    private static boolean omiteNulos(@Nullable JsonInclude anotacao) {
        return anotacao != null && anotacao.value() == JsonInclude.Include.NON_NULL;
    }

    private static boolean anotadoComNullable(RecordComponent componente) {
        return componente.getAnnotatedType().isAnnotationPresent(Nullable.class);
    }

    private static String nome(Schema esquema) {
        return esquema.get$ref().substring(esquema.get$ref().lastIndexOf('/') + 1);
    }
}
