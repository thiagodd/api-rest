package com.bazar.clientes.api;

import com.bazar.clientes.dominio.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

/**
 * Corpo do POST e do PUT de um endereço. O numero é texto: "S/N" e "120-A" são números de endereço válidos.
 * O principal é opcional (ausente = false); um boolean primitivo ausente seria um erro no Jackson 3 (06.01).
 */
public record EnderecoRequest(
        @NotBlank @Pattern(regexp = "\\d{8}", message = "{bazar.validacao.cep.message}") String cep,
        @NotBlank @Size(max = 120) String logradouro,
        @NotBlank @Size(max = 10) String numero,
        @Nullable @Size(max = 60) String complemento,
        @NotBlank @Size(max = 60) String cidade,
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "{bazar.validacao.uf.message}") String uf,
        @Nullable Boolean principal) {

    Endereco.Dados paraDados() {
        return new Endereco.Dados(cep, logradouro, numero, complemento, cidade, uf, Boolean.TRUE.equals(principal));
    }
}
