package com.bazar.relatorios.api;

import java.util.UUID;

import com.bazar.compartilhado.operacoes.LocalDoResultado;
import com.bazar.relatorios.dominio.RelatorioVendasService;

import org.springframework.stereotype.Component;

/** O resultado de uma operação RELATORIO_VENDAS é o relatório de mesmo id (21.02). */
@Component
class ResultadoDoRelatorio implements LocalDoResultado {

    @Override
    public String tipo() {
        return RelatorioVendasService.TIPO;
    }

    @Override
    public String caminho(UUID operacao) {
        return "/v1/relatorios/" + operacao;
    }
}
