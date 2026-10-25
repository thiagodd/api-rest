package com.bazar.compartilhado.erros;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * A requisição trouxe parâmetros de query que o endpoint não conhece (400). Sem esse erro, um parâmetro com o
 * nome errado (apenas-raizes em vez de apenasRaizes) seria ignorado em silêncio (12.02).
 */
public class ParametroDesconhecidoException extends ExcecaoDeDominio {

    public ParametroDesconhecidoException(SortedSet<String> desconhecidos, SortedSet<String> aceitos) {
        super("parametro-desconhecido",
                Map.of("parametros", List.copyOf(desconhecidos), "parametrosAceitos", List.copyOf(aceitos)),
                String.join(", ", desconhecidos));
    }
}
