package com.bazar.relatorios.dominio;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Um relatório de vendas pronto: o resultado da operação de mesmo id (21.02). O download do arquivo é da 22.02.
 */
public record RelatorioVendas(UUID id, PeriodoDeVendas periodo, Path arquivo, long tamanhoEmBytes, Instant geradoEm) {
}
