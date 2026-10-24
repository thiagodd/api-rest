package com.bazar.compartilhado.validacao;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(@Nullable String cpf, ConstraintValidatorContext contexto) {
        if (cpf == null) {
            return true; // obrigatoriedade é com @NotBlank
        }
        if (!cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false; // formato errado ou 111.111.111-11, que passaria na conta
        }
        return digitoVerificador(cpf, 9) == cpf.charAt(9) - '0'
                && digitoVerificador(cpf, 10) == cpf.charAt(10) - '0';
    }

    // Soma os primeiros n dígitos com pesos n+1, n, ..., 2; o dígito é (soma × 10) mod 11, e 10 vira 0.
    private static int digitoVerificador(String cpf, int n) {
        int soma = 0;
        for (int i = 0; i < n; i++) {
            soma += (cpf.charAt(i) - '0') * (n + 1 - i);
        }
        int resto = soma * 10 % 11;
        return resto == 10 ? 0 : resto;
    }
}
