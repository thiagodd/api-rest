package com.bazar.compartilhado.erros;

import java.net.URI;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Substitui o BasicErrorController do Spring Boot. Atende o /error, para onde o servidor encaminha os erros
 * que não passaram pelo Spring MVC (um filtro que chama sendError, por exemplo), e responde no mesmo formato.
 */
@RestController
public class ErroForaDoMvcController implements ErrorController {

    @RequestMapping("${server.error.path:/error}")
    ResponseEntity<ProblemDetail> erro(HttpServletRequest request) {
        var codigo = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        var status = HttpStatus.resolve(codigo instanceof Integer valor ? valor : 500);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        var problema = ProblemDetail.forStatus(status);
        problema.setTitle(status.getReasonPhrase());
        problema.setProperty("codigo", status.name());
        if (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) instanceof String caminho) {
            problema.setInstance(URI.create(caminho));
        }
        problema.setProperty("traceId", TratadorGlobalDeErros.novoTraceId());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }
}
