package com.bazar.clientes.api;

import com.bazar.clientes.dominio.Cliente;
import com.bazar.compartilhado.Canal;
import com.bazar.compartilhado.validacao.Cpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record CriarClienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Cpf String cpf,
        @Nullable @Pattern(regexp = "\\+\\d{10,13}") String telefone) {

    Cliente paraCliente(Canal canal) {
        return Cliente.novo(nome, email, cpf, telefone, canal);
    }
}
