package com.bazar.compartilhado.erros;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * O parâmetro sort pediu uma ordenação que o endpoint não oferece (400): um campo fora da lista, um campo repetido,
 * ignorecase ou campos demais (13.02). O corpo traz a lista dos campos aceitos e o máximo de campos.
 */
public class OrdenacaoInvalidaException extends ExcecaoDeDominio {

    public OrdenacaoInvalidaException(List<String> recusados, SortedSet<String> aceitos, int maximoDeCampos) {
        super(TipoProblema.ORDENACAO_INVALIDA,
                Map.of("ordenacao", List.copyOf(recusados), "camposAceitos", List.copyOf(aceitos),
                        "maximoDeCampos", maximoDeCampos),
                String.join("; ", recusados), maximoDeCampos, String.join(", ", aceitos));
    }
}
