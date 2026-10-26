package com.bazar.compartilhado.operacoes;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.bazar.compartilhado.erros.Problemas;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.RegraDeNegocioException;
import com.bazar.compartilhado.erros.TipoProblema;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.Nullable;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.bazar.compartilhado.web.RespostasDeErro.NAO_ENCONTRADO;

/**
 * O recurso de status das operações longas (21.02): o cliente que recebeu um 202 consulta aqui até a operação
 * terminar.
 */
@RestController
@RequestMapping("/v1/operacoes")
@Tag(name = "operacoes", description = "Operações longas, executadas fora da requisição")
public class OperacaoController {

    /** De quanto em quanto tempo o cliente deve consultar uma operação que ainda não terminou (Retry-After). */
    public static final Duration INTERVALO_DE_CONSULTA = Duration.ofSeconds(2);

    private final FilaDeOperacoes fila;
    private final Map<String, LocalDoResultado> resultados;
    private final MessageSource mensagens;

    public OperacaoController(FilaDeOperacoes fila, List<LocalDoResultado> resultados, MessageSource mensagens) {
        this.fila = fila;
        this.resultados = resultados.stream().collect(Collectors.toMap(LocalDoResultado::tipo, Function.identity()));
        this.mensagens = mensagens;
    }

    /** O caminho do recurso de status, para o Location do 202. */
    public static String caminho(UUID operacao) {
        return "/v1/operacoes/" + operacao;
    }

    // Em andamento: 200 com Retry-After. Concluída: 303 See Other para o resultado, com o estado no corpo. Falhou: 200
    // com o erro no corpo; o GET deu certo, quem falhou foi a operação.
    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "A operação está em andamento (com Retry-After) ou falhou (com erro)",
            headers = @Header(name = "Retry-After", description = "Segundos até a próxima consulta",
                    schema = @Schema(type = "integer")))
    @ApiResponse(responseCode = "303", description = "A operação terminou; o Location aponta o resultado",
            headers = @Header(name = "Location", description = "O recurso com o resultado",
                    schema = @Schema(type = "string")))
    @ApiResponse(responseCode = "404", ref = NAO_ENCONTRADO)
    public ResponseEntity<OperacaoResponse> detalhar(@PathVariable UUID id) {
        var operacao = fila.buscar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TipoProblema.OPERACAO_NAO_ENCONTRADA, id));
        return switch (operacao.status()) {
            case PENDENTE, EM_EXECUCAO -> ResponseEntity.ok()
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(INTERVALO_DE_CONSULTA.toSeconds()))
                    .body(resposta(operacao, null));
            case CONCLUIDA -> {
                var resultado = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(resultados.get(operacao.tipo()).caminho(id))
                        .build().toUri();
                yield ResponseEntity.status(HttpStatus.SEE_OTHER).location(resultado)
                        .body(new OperacaoResponse(id, operacao.tipo(), operacao.status(), operacao.criadaEm(),
                                operacao.atualizadaEm(), resultado, null));
            }
            // A mensagem da exceção fica na tabela e no log, para quem opera o Bazar. O cliente recebe um problema
            // genérico: uma mensagem interna pode revelar SQL, caminhos de arquivo ou dados de outras pessoas.
            case FALHOU -> ResponseEntity.ok().body(resposta(operacao, Problemas.de(
                    new RegraDeNegocioException(TipoProblema.OPERACAO_FALHOU, Map.of()), mensagens,
                    LocaleContextHolder.getLocale())));
        };
    }

    private static OperacaoResponse resposta(Operacao operacao, @Nullable ProblemDetail erro) {
        return new OperacaoResponse(operacao.id(), operacao.tipo(), operacao.status(), operacao.criadaEm(),
                operacao.atualizadaEm(), null, erro);
    }
}
