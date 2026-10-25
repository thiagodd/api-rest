package com.bazar.compartilhado.erros;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.bazar.compartilhado.web.Depreciacao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Converte toda exceção que sai de um controller num Problem Details (RFC 9457) no formato do Bazar (08.01).
 * As exceções do Spring MVC (405, 406, 415, corpo ilegível...) são tratadas pela classe-mãe.
 */
@RestControllerAdvice
public class TratadorGlobalDeErros extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    // Exceções do Spring que ganham um type do Bazar; as demais ficam com about:blank.
    private static final Map<Class<? extends Exception>, TipoProblema> TIPOS_DO_SPRING = Map.of(
            HttpMessageNotReadableException.class, TipoProblema.REQUISICAO_INVALIDA,
            TypeMismatchException.class, TipoProblema.REQUISICAO_INVALIDA,
            HttpMediaTypeNotAcceptableException.class, TipoProblema.FORMATO_NAO_ACEITO,
            HttpMediaTypeNotSupportedException.class, TipoProblema.FORMATO_NAO_SUPORTADO,
            InvalidApiVersionException.class, TipoProblema.VERSAO_NAO_SUPORTADA);

    private final Clock relogio;

    // O Clock vem da ConfiguracaoDoRelogio (15.01). Nos testes de fatia (@WebMvcTest), que não a carregam, vale o
    // relógio do sistema.
    @Autowired
    public TratadorGlobalDeErros(ObjectProvider<Clock> relogio) {
        this.relogio = relogio.getIfAvailable(Clock::systemUTC);
    }

    // Para os testes standalone do MockMvc, que criam o tratador à mão.
    public TratadorGlobalDeErros() {
        this.relogio = Clock.systemUTC();
    }

    // Depois do sunset, os handlers da versão saem do código. Enquanto outra versão do recurso existe, o Spring não
    // acha handler para a antiga (404); quando a versão sai da lista de suportadas, ele a recusa (400). Nos dois
    // casos, para uma versão que existiu, o Bazar responde 410 Gone, com o guia de migração (18.04).
    @ExceptionHandler(InvalidApiVersionException.class)
    ResponseEntity<Object> versaoRecusada(InvalidApiVersionException ex, WebRequest request) throws Exception {
        var desligada = desligada(request);
        return desligada != null ? desligada : handleException(ex, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var desligada = desligada(request);
        return desligada != null ? desligada : super.handleNoResourceFoundException(ex, headers, status, request);
    }

    private @Nullable ResponseEntity<Object> desligada(WebRequest request) {
        return Depreciacao.desligada(caminho(request), relogio.instant())
                .map(depreciacao -> deDominio(new VersaoDesligadaException(depreciacao), request))
                .orElse(null);
    }

    // Toda exceção de domínio aponta um tipo do catálogo (TipoProblema), e o tipo define o status (16.01).
    @ExceptionHandler(ExcecaoDeDominio.class)
    ResponseEntity<Object> deDominio(ExcecaoDeDominio ex, WebRequest request) {
        var problema = Problemas.de(ex, getMessageSource(), LocaleContextHolder.getLocale());
        return handleExceptionInternal(ex, problema, new HttpHeaders(), ex.getTipo().status(), request);
    }

    // Lançada no commit, quando o UPDATE com "where versao=?" não encontra a linha: outra transação
    // alterou o registro entre a leitura e a gravação. A mensagem do Spring cita a entidade; não vai para o cliente.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<Object> conflitoDeVersao(OptimisticLockingFailureException ex, WebRequest request) {
        return deDominio(new ConflitoDeVersaoException(), request);
    }

    // Um record validado à mão, como o resultado de um PATCH (14.02): o mesmo 422 de um corpo inválido.
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> violacoes(ConstraintViolationException ex, WebRequest request) {
        var erros = ex.getConstraintViolations().stream()
                .map(violacao -> new ErroDeCampo(violacao.getPropertyPath().toString(), mensagem(violacao.getMessage())))
                .sorted(Comparator.comparing(ErroDeCampo::campo))
                .toList();
        return validacao(ex, erros, new HttpHeaders(), request);
    }

    @ExceptionHandler(CampoInvalidoException.class)
    ResponseEntity<Object> campoInvalido(CampoInvalidoException ex, WebRequest request) {
        var fonte = getMessageSource();
        var mensagem = fonte != null
                ? fonte.getMessage(ex.getCodigoDaMensagem(), null, ex.getCodigoDaMensagem(), LocaleContextHolder.getLocale())
                : ex.getCodigoDaMensagem();
        return validacao(ex, List.of(new ErroDeCampo(ex.getCampo(), mensagem(mensagem))), new HttpHeaders(), request);
    }

    // Último recurso: nada de mensagem nem stack trace para o cliente; o detalhe fica no log, ligado pelo traceId.
    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> inesperado(Exception ex, WebRequest request) {
        var problema = ErrorResponse.builder(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.")
                .detailMessageCode("problemDetail.erro-inesperado")
                .build()
                .updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        return handleExceptionInternal(ex, problema, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // Um campo desconhecido no corpo de uma busca chega embrulhado pelo Jackson (BuscaDeProdutos, 13.01).
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ex.getMostSpecificCause() instanceof ParametroDesconhecidoException desconhecido) {
            return deDominio(desconhecido, request);
        }
        // Um valor fora do enum ("motivo": "TALVEZ") é JSON bem formado com um valor inválido: 422 no campo, com a
        // lista de valores aceitos, como qualquer outra validação (15.04).
        if (ex.getMostSpecificCause() instanceof InvalidFormatException invalido && invalido.getTargetType() != null
                && invalido.getTargetType().isEnum()) {
            return valorForaDoEnum(invalido, request);
        }
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    private ResponseEntity<Object> valorForaDoEnum(InvalidFormatException ex, WebRequest request) {
        var mensagem = mensagemDoEnum(ex.getTargetType());
        return validacao(ex, List.of(new ErroDeCampo(caminho(ex), mensagem)), new HttpHeaders(), request);
    }

    private String mensagemDoEnum(Class<?> tipo) {
        var aceitos = Arrays.stream(tipo.getEnumConstants())
                .map(valor -> ((Enum<?>) valor).name())
                .collect(Collectors.joining(", "));
        var fonte = getMessageSource();
        return fonte != null
                ? fonte.getMessage("bazar.validacao.enum", new Object[] {aceitos}, LocaleContextHolder.getLocale())
                : aceitos;
    }

    // O enum de um parâmetro que não converteu. Numa lista (status=ATIVO&status=ESGOTADO), o tipo pedido é a List,
    // e o enum está na falha de conversão do elemento.
    private static Optional<Class<?>> enumNaoConvertido(FieldError falha) {
        if (!falha.contains(TypeMismatchException.class)) {
            return Optional.empty();
        }
        var incompativel = falha.unwrap(TypeMismatchException.class);
        Class<?> alvo = incompativel.getCause() instanceof ConversionFailedException conversao
                ? conversao.getTargetType().getType()
                : incompativel.getRequiredType();
        return alvo != null && alvo.isEnum() ? Optional.of(alvo) : Optional.empty();
    }

    // O caminho do campo no corpo, no formato dos outros erros: itens[0].produtoId.
    private static String caminho(JacksonException ex) {
        var caminho = new StringBuilder();
        for (var parte : ex.getPath()) {
            if (parte.getPropertyName() != null) {
                caminho.append(caminho.isEmpty() ? "" : ".").append(parte.getPropertyName());
            } else if (parte.getIndex() >= 0) {
                caminho.append('[').append(parte.getIndex()).append(']');
            }
        }
        return caminho.toString();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // Num @ModelAttribute, um valor que nem converte (precoMin=abc) vira erro de binding: 400, como um
        // @PathVariable malformado, sem a mensagem do conversor, que cita tipos Java e ecoa o valor. Um valor fora
        // de um enum (status=ESGOTADO) tem o tipo certo, texto, e é tratado como no corpo: 422 com os valores
        // aceitos (16.01).
        var falhas = ex.getBindingResult().getFieldErrors().stream().filter(FieldError::isBindingFailure).toList();
        if (falhas.stream().anyMatch(falha -> enumNaoConvertido(falha).isEmpty())) {
            var naoConvertidos = falhas.stream().map(FieldError::getField).distinct().sorted().toList();
            return valorNaoConvertido(ex, naoConvertidos, request);
        }
        var erros = new ArrayList<ErroDeCampo>();
        for (var falha : falhas) {
            erros.add(new ErroDeCampo(falha.getField(), mensagemDoEnum(enumNaoConvertido(falha).orElseThrow())));
        }
        ex.getBindingResult().getAllErrors().stream()
                .filter(erro -> !(erro instanceof FieldError campo && campo.isBindingFailure()))
                .map(erro -> new ErroDeCampo(
                        erro instanceof FieldError campo ? campo.getField() : erro.getObjectName(),
                        mensagem(erro.getDefaultMessage())))
                .forEach(erros::add);
        return validacao(ex, erros, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var erros = new ArrayList<ErroDeCampo>();
        for (var resultado : ex.getParameterValidationResults()) {
            // Um objeto validado com @Valid (um record @ModelAttribute, como PaginaPedida): o campo é o do objeto
            // (page), e não o nome do parâmetro no método (pagina), que o cliente nunca viu (13.03).
            if (resultado instanceof ParameterErrors errosDoObjeto) {
                for (var erro : errosDoObjeto.getFieldErrors()) {
                    erros.add(new ErroDeCampo(erro.getField(), mensagem(erro.getDefaultMessage())));
                }
                continue;
            }
            var parametro = resultado.getMethodParameter().getParameterName();
            for (var erro : resultado.getResolvableErrors()) {
                erros.add(new ErroDeCampo(String.valueOf(parametro), mensagem(erro.getDefaultMessage())));
            }
        }
        return validacao(ex, erros, headers, request);
    }

    // Ponto por onde passam todas as respostas de erro: completa o Problem Details com o que é comum a todas.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        // As exceções do Spring que já são ErrorResponse (405, 406, 415...) chegam sem corpo: ele é montado aqui.
        if (body == null && ex instanceof ErrorResponse erro) {
            body = erro.updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        }
        // Uma ResponseStatusException com reason fica com o reason (em inglês) como detail, mesmo com a chave da classe
        // no messages.properties: a mensagem da versão é resolvida aqui (18.03).
        if (body instanceof ProblemDetail problema && ex instanceof InvalidApiVersionException versao) {
            problema.setDetail(mensagem("problemDetail.versao-recusada", versao.getVersion()));
        }
        if (body instanceof ProblemDetail problema) {
            completar(problema, ex, status, request);
        }
        // O title, o detail e as mensagens dos erros estão no idioma pedido: a resposta diz qual é, e um cache sabe
        // que ela muda com o Accept-Language (15.05).
        var cabecalhos = new HttpHeaders();
        cabecalhos.addAll(headers);
        cabecalhos.setContentLanguage(LocaleContextHolder.getLocale());
        cabecalhos.add(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
        return super.handleExceptionInternal(ex, body, cabecalhos, status, request);
    }

    private String mensagem(String codigo, Object... argumentos) {
        var mensagens = getMessageSource();
        return mensagens != null
                ? mensagens.getMessage(codigo, argumentos, codigo, LocaleContextHolder.getLocale())
                : codigo;
    }

    private ResponseEntity<Object> valorNaoConvertido(Exception ex, List<String> campos, WebRequest request) {
        var problema = ErrorResponse.builder(ex, HttpStatus.BAD_REQUEST, "Invalid request.")
                .type(TipoProblema.REQUISICAO_INVALIDA.uri())
                .titleMessageCode("problemDetail.title.org.springframework.web.method.annotation.MethodArgumentTypeMismatchException")
                .detailMessageCode("problemDetail.org.springframework.beans.TypeMismatchException")
                .detailMessageArguments(String.join(", ", campos))
                .build()
                .updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        problema.setProperty("parametros", campos);
        return handleExceptionInternal(ex, problema, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<Object> validacao(Exception ex, List<ErroDeCampo> erros, HttpHeaders headers, WebRequest request) {
        var status = HttpStatus.UNPROCESSABLE_CONTENT;
        var problema = ErrorResponse.builder(ex, status, erros.size() + " invalid field(s).")
                .type(TipoProblema.VALIDACAO.uri())
                .titleMessageCode("problemDetail.title.validacao")
                .detailMessageCode("problemDetail.validacao")
                .detailMessageArguments(erros.size())
                .build()
                .updateAndGetBody(getMessageSource(), LocaleContextHolder.getLocale());
        problema.setProperty("erros", erros.stream().sorted(Comparator.comparing(ErroDeCampo::campo)).toList());
        return handleExceptionInternal(ex, problema, headers, status, request);
    }

    private void completar(ProblemDetail problema, Exception ex, HttpStatusCode status, WebRequest request) {
        var tipoDoSpring = TIPOS_DO_SPRING.entrySet().stream()
                .filter(e -> e.getKey().isInstance(ex))
                .map(Map.Entry::getValue)
                .findFirst();
        if (tipoDoSpring.isPresent() && isAboutBlank(problema)) {
            problema.setType(tipoDoSpring.get().uri());
        }
        problema.setProperty("codigo", codigo(problema, status));
        problema.setInstance(URI.create(caminho(request)));   // o Spring 7.1 deixa de preencher sozinho
        var traceId = novoTraceId();
        problema.setProperty("traceId", traceId);
        if (status.is5xxServerError()) {
            log.error("Erro inesperado [traceId={}]", traceId, ex);
        }
        else {
            log.debug("{} {} [traceId={}]: {}", status.value(), problema.getType(), traceId, ex.getMessage());
        }
    }

    // O codigo espelha o type: produto-nao-encontrado → PRODUTO_NAO_ENCONTRADO; about:blank usa o nome do status.
    private static String codigo(ProblemDetail problema, HttpStatusCode status) {
        if (isAboutBlank(problema)) {
            var conhecido = HttpStatus.resolve(status.value());
            return conhecido != null ? conhecido.name() : "HTTP_" + status.value();
        }
        var tipo = String.valueOf(problema.getType());
        return tipo.substring(tipo.lastIndexOf('/') + 1).replace('-', '_').toUpperCase(Locale.ROOT);
    }

    // No Spring 7 o type padrão é null (ausente no JSON), que a RFC lê como about:blank.
    private static boolean isAboutBlank(ProblemDetail problema) {
        var tipo = problema.getType();
        return tipo == null || "about:blank".equals(tipo.toString());
    }

    private static String caminho(WebRequest request) {
        var servlet = ((NativeWebRequest) request).getNativeRequest(HttpServletRequest.class);
        return servlet != null ? servlet.getRequestURI() : "";
    }

    // Provisório: em 28.02 o traceId passa a ser o do OpenTelemetry, o mesmo dos logs e dos traces.
    static String novoTraceId() {
        var bytes = new byte[16];
        ThreadLocalRandom.current().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String mensagem(@Nullable String mensagem) {
        return mensagem != null ? mensagem : "";
    }

    public record ErroDeCampo(String campo, String mensagem) {
    }
}
