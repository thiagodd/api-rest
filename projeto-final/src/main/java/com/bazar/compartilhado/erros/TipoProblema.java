package com.bazar.compartilhado.erros;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import static com.bazar.compartilhado.erros.TipoProblema.Acao.AGUARDAR_E_REPETIR;
import static com.bazar.compartilhado.erros.TipoProblema.Acao.CORRIGIR_A_REQUISICAO;
import static com.bazar.compartilhado.erros.TipoProblema.Acao.MOSTRAR_AO_USUARIO;
import static com.bazar.compartilhado.erros.TipoProblema.Acao.RELER_E_REPETIR;

/**
 * O catálogo de problemas do Bazar (16.01): todo type que a API devolve está aqui, com o status e o que o cliente
 * deve fazer ao recebê-lo. O nome da constante é o codigo do Problem Details; o slug (produto-nao-encontrado) é o
 * final da URI do type e a chave das mensagens (problemDetail.title.produto-nao-encontrado).
 * <p>
 * Um tipo novo é uma mudança compatível só se os clientes tratarem um codigo desconhecido pelo status (15.04). Um
 * tipo existente não muda de status nem de significado.
 */
public enum TipoProblema {

    // 400: a requisição não segue a forma do contrato (sintaxe, tipo, parâmetro desconhecido).
    REQUISICAO_INVALIDA(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    PARAMETRO_DESCONHECIDO(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    VALOR_NAO_ACEITO(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    ORDENACAO_INVALIDA(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    CURSOR_INVALIDO(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    PATCH_INVALIDO(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    VERSAO_NAO_SUPORTADA(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    CHAVE_DE_IDEMPOTENCIA_INVALIDA(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),
    // A assinatura HMAC ou o timestamp de um webhook recebido não conferem (21.03). 400, e não 401: um 401 exige o
    // header WWW-Authenticate com um esquema de autenticação, e o Standard Webhooks não define um.
    WEBHOOK_NAO_VERIFICADO(HttpStatus.BAD_REQUEST, CORRIGIR_A_REQUISICAO),

    // 404: o recurso não existe (ou o cliente não pode saber que existe).
    PRODUTO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, MOSTRAR_AO_USUARIO),
    CATEGORIA_NAO_ENCONTRADA(HttpStatus.NOT_FOUND, MOSTRAR_AO_USUARIO),
    CLIENTE_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, MOSTRAR_AO_USUARIO),
    ENDERECO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, MOSTRAR_AO_USUARIO),
    PEDIDO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, MOSTRAR_AO_USUARIO),
    OPERACAO_NAO_ENCONTRADA(HttpStatus.NOT_FOUND, CORRIGIR_A_REQUISICAO),
    RELATORIO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, CORRIGIR_A_REQUISICAO),
    ASSINATURA_WEBHOOK_NAO_ENCONTRADA(HttpStatus.NOT_FOUND, CORRIGIR_A_REQUISICAO),
    IMAGEM_NAO_ENCONTRADA(HttpStatus.NOT_FOUND, MOSTRAR_AO_USUARIO),

    // 410: a versão do recurso existiu, mas foi desligada depois do sunset (18.04).
    VERSAO_DESLIGADA(HttpStatus.GONE, CORRIGIR_A_REQUISICAO),

    // 406 e 415: negociação de formato (02.05).
    FORMATO_NAO_ACEITO(HttpStatus.NOT_ACCEPTABLE, CORRIGIR_A_REQUISICAO),
    FORMATO_NAO_SUPORTADO(HttpStatus.UNSUPPORTED_MEDIA_TYPE, CORRIGIR_A_REQUISICAO),
    // A requisição veio com um Content-Encoding que o Bazar não descomprime (22.03).
    CODIFICACAO_NAO_SUPORTADA(HttpStatus.UNSUPPORTED_MEDIA_TYPE, CORRIGIR_A_REQUISICAO),
    // O conteúdo do arquivo (os primeiros bytes, não o Content-Type declarado) não é de um formato aceito (22.01).
    IMAGEM_NAO_ACEITA(HttpStatus.UNSUPPORTED_MEDIA_TYPE, MOSTRAR_AO_USUARIO),

    // 413: o arquivo ou a requisição passou do limite de tamanho (22.01).
    ARQUIVO_GRANDE_DEMAIS(HttpStatus.CONTENT_TOO_LARGE, MOSTRAR_AO_USUARIO),
    // O corpo, depois de descomprimido, passou do limite (22.03).
    CORPO_GRANDE_DEMAIS(HttpStatus.CONTENT_TOO_LARGE, CORRIGIR_A_REQUISICAO),

    // 409: a requisição é válida, mas conflita com o estado atual do recurso.
    SKU_DUPLICADO(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    CATEGORIA_COM_PRODUTOS(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    CATEGORIA_COM_SUBCATEGORIAS(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    ENDERECO_PRINCIPAL_OBRIGATORIO(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    LIMITE_DE_ENDERECOS(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    LIMITE_DE_IMAGENS(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    TRANSICAO_INVALIDA(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    ESTOQUE_INSUFICIENTE(HttpStatus.CONFLICT, MOSTRAR_AO_USUARIO),
    TESTE_DO_PATCH_FALHOU(HttpStatus.CONFLICT, RELER_E_REPETIR),
    // A primeira requisição com esta Idempotency-Key ainda está em andamento (21.01).
    REQUISICAO_EM_ANDAMENTO(HttpStatus.CONFLICT, AGUARDAR_E_REPETIR),
    CONFLITO_DE_VERSAO(HttpStatus.CONFLICT, RELER_E_REPETIR),

    // 500: não é a resposta de uma requisição, e sim o erro de uma operação assíncrona que falhou, no campo erro do
    // recurso da operação (21.02). O GET da operação em si responde 200.
    OPERACAO_FALHOU(HttpStatus.INTERNAL_SERVER_ERROR, AGUARDAR_E_REPETIR),

    // 429: o cliente passou do limite de uma política de rate limiting (23.01). O Retry-After diz quando há ficha.
    LIMITE_DE_REQUISICOES(HttpStatus.TOO_MANY_REQUESTS, AGUARDAR_E_REPETIR),

    // 503: o servidor está sem vaga para esta requisição agora (22.02). Não é um defeito; o Retry-After diz quando
    // tentar de novo.
    CAPACIDADE_ESGOTADA(HttpStatus.SERVICE_UNAVAILABLE, AGUARDAR_E_REPETIR),

    // 412 e 428: requisições condicionais (20.03). O If-Match não bate com a versão atual, ou a escrita veio sem
    // nenhuma precondição.
    PRECONDICAO_FALHOU(HttpStatus.PRECONDITION_FAILED, RELER_E_REPETIR),
    PRECONDICAO_OBRIGATORIA(HttpStatus.PRECONDITION_REQUIRED, CORRIGIR_A_REQUISICAO),

    // 422: a requisição tem a forma certa, mas um ou mais campos violam as regras (a lista vem em erros).
    VALIDACAO(HttpStatus.UNPROCESSABLE_CONTENT, CORRIGIR_A_REQUISICAO),
    // A mesma Idempotency-Key com outro corpo (21.01): uma chave identifica uma requisição só.
    CHAVE_DE_IDEMPOTENCIA_REUTILIZADA(HttpStatus.UNPROCESSABLE_CONTENT, CORRIGIR_A_REQUISICAO);

    // Os erros que são só o status HTTP (404 de rota inexistente, 405, 500) usam about:blank, como a RFC 9457
    // prevê, e o codigo é o nome do status. Eles estão no catálogo publicado, mas não aqui.

    static final String BASE = "https://api.bazar.example/problemas/";

    /** O que um cliente bem escrito faz ao receber o problema. */
    public enum Acao {
        /** Repetir a mesma requisição dá o mesmo erro: o código do cliente precisa mudar. */
        CORRIGIR_A_REQUISICAO,
        /** O erro é uma situação do negócio: o usuário decide (escolher outro SKU, reduzir a quantidade). */
        MOSTRAR_AO_USUARIO,
        /** O recurso mudou desde a leitura: buscar o estado atual, reaplicar a mudança e enviar de novo. */
        RELER_E_REPETIR,
        /** Falha temporária: repetir depois de um tempo, com backoff, se a operação for idempotente. */
        AGUARDAR_E_REPETIR
    }

    private final HttpStatus status;
    private final Acao acao;

    TipoProblema(HttpStatus status, Acao acao) {
        this.status = status;
        this.acao = acao;
    }

    public HttpStatus status() {
        return status;
    }

    public Acao acao() {
        return acao;
    }

    /** produto-nao-encontrado: o final da URI e a chave das mensagens. */
    public String slug() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public URI uri() {
        return URI.create(BASE + slug());
    }

    public static Optional<TipoProblema> doSlug(String slug) {
        return Arrays.stream(values()).filter(tipo -> tipo.slug().equals(slug)).findFirst();
    }
}
