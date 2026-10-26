package com.bazar.compartilhado.web;

/**
 * As respostas de erro reutilizáveis do contrato, registradas em ConfiguracaoOpenApi, para usar em
 * {@code @ApiResponse(responseCode = "404", ref = RespostasDeErro.NAO_ENCONTRADO)} (17.02).
 */
public final class RespostasDeErro {

    public static final String NAO_ENCONTRADO = "#/components/responses/NaoEncontrado";
    public static final String CONFLITO = "#/components/responses/Conflito";
    public static final String VALIDACAO = "#/components/responses/Validacao";
    public static final String PRECONDICAO_FALHOU = "#/components/responses/PrecondicaoFalhou";
    public static final String PRECONDICAO_OBRIGATORIA = "#/components/responses/PrecondicaoObrigatoria";

    private RespostasDeErro() {
    }
}
