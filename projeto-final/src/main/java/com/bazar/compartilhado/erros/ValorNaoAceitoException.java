package com.bazar.compartilhado.erros;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Um parâmetro com uma lista de nomes (fields, embed) trouxe um nome que o endpoint não oferece (400, 13.05).
 * O corpo traz o parâmetro, os nomes recusados e os aceitos.
 */
public class ValorNaoAceitoException extends ExcecaoDeDominio {

    public ValorNaoAceitoException(String parametro, SortedSet<String> recusados, SortedSet<String> aceitos) {
        super("valor-nao-aceito",
                Map.of("parametro", parametro, "recusados", List.copyOf(recusados), "aceitos", List.copyOf(aceitos)),
                parametro, String.join(", ", recusados), String.join(", ", aceitos));
    }
}
