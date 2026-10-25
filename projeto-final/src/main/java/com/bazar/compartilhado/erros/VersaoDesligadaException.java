package com.bazar.compartilhado.erros;

import java.util.Map;

import com.bazar.compartilhado.web.Depreciacao;

/** Uma versão de um recurso que já passou do sunset e saiu do código (18.04): 410 Gone, com o guia de migração. */
public class VersaoDesligadaException extends ExcecaoDeDominio {

    public VersaoDesligadaException(Depreciacao depreciacao) {
        super(TipoProblema.VERSAO_DESLIGADA,
                Map.of("guiaDeMigracao", depreciacao.guia().toString(), "desligadaEm", depreciacao.sunset().toString()),
                "v" + depreciacao.versao(), depreciacao.sunset().toString());
    }
}
