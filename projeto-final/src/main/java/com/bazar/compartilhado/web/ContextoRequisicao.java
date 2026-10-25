package com.bazar.compartilhado.web;

import java.util.Locale;

import com.bazar.compartilhado.Canal;

/**
 * Dados de contexto que vários endpoints usam. Basta declarar um parâmetro deste tipo no controller:
 * o ContextoRequisicaoResolver o preenche a partir da requisição.
 */
public record ContextoRequisicao(Canal canal, Locale idioma) {
}
