package com.bazar.compartilhado.web;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.bazar.compartilhado.erros.ParametroDesconhecidoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Recusa parâmetros de query que o endpoint não declara (12.02). Os nomes aceitos vêm da assinatura do método:
 * cada @RequestParam, explícito ou implícito (um tipo simples sem anotação), e os componentes de um record
 * recebido com @ModelAttribute (13.01). Um @RequestParam Map aceita tudo.
 */
public class ParametrosDeConsultaInterceptor implements HandlerInterceptor {

    // Set.of("*") = o método aceita qualquer parâmetro
    private final Map<Method, Set<String>> aceitosPorMetodo = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod metodo) || request.getQueryString() == null) {
            return true;
        }
        var aceitos = aceitosPorMetodo.computeIfAbsent(metodo.getMethod(), m -> aceitos(metodo));
        if (aceitos.contains("*")) {
            return true;
        }
        var desconhecidos = new TreeSet<String>();
        for (var nome : request.getParameterMap().keySet()) {
            if (!aceitos.contains(nome)) {
                desconhecidos.add(nome);
            }
        }
        if (!desconhecidos.isEmpty()) {
            throw new ParametroDesconhecidoException(desconhecidos, new TreeSet<>(aceitos));
        }
        return true;
    }

    private static Set<String> aceitos(HandlerMethod metodo) {
        SortedSet<String> nomes = new TreeSet<>();
        for (var parametro : metodo.getMethodParameters()) {
            var anotacao = parametro.getParameterAnnotation(RequestParam.class);
            if (anotacao != null && Map.class.isAssignableFrom(parametro.getParameterType())) {
                return Set.of("*");
            }
            if (anotacao != null) {
                nomes.add(StringUtils.hasText(anotacao.name()) ? anotacao.name() : nomeNoCodigo(parametro));
            }
            else if (parametro.hasParameterAnnotation(ModelAttribute.class)) {
                nomes.addAll(componentes(parametro.getParameterType()));
            }
            else if (parametro.getParameterAnnotations().length == 0
                    && BeanUtils.isSimpleProperty(parametro.getParameterType())) {
                nomes.add(nomeNoCodigo(parametro));
            }
        }
        return Set.copyOf(nomes);
    }

    // Só records: os componentes são exatamente os parâmetros que o Spring consegue preencher.
    private static List<String> componentes(Class<?> tipo) {
        if (!tipo.isRecord()) {
            throw new IllegalStateException("@ModelAttribute sem record não é suportado: " + tipo.getName());
        }
        return Arrays.stream(tipo.getRecordComponents()).map(RecordComponent::getName).toList();
    }

    // Depende de -parameters na compilação, que o plugin do Spring Boot configura.
    private static String nomeNoCodigo(MethodParameter parametro) {
        var nome = parametro.getParameterName();
        if (nome == null) {
            throw new IllegalStateException("Compile com -parameters: " + parametro);
        }
        return nome;
    }
}
