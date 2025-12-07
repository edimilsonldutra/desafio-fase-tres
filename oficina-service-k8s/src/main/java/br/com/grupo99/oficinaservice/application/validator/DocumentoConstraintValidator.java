package br.com.grupo99.oficinaservice.application.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DocumentoConstraintValidator implements ConstraintValidator<DocumentoValido, String> {

    private static final String PADRAO_CARACTERES_VALIDOS = "[\\d./-]+";
    private static final int TAMANHO_CPF = 11;
    private static final int TAMANHO_CNPJ = 14;

    @Override
    public boolean isValid(String documento, ConstraintValidatorContext context) {
        if (documento == null || documento.isBlank()) {
            return true; // @NotBlank se responsabiliza por isso
        }

        if (!documento.matches(PADRAO_CARACTERES_VALIDOS)) {
            return false;
        }

        String numeros = limparDocumento(documento);

        return switch (numeros.length()) {
            case TAMANHO_CPF -> isValidCPF(numeros);
            case TAMANHO_CNPJ -> isValidCNPJ(numeros);
            default -> false;
        };
    }

    private String limparDocumento(String documento) {
        return documento.replaceAll("[^\\d]", "");
    }

    private boolean isValidCPF(String cpf) {
        if (cpf.length() != TAMANHO_CPF || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int digito1 = calcularDigitoVerificador(cpf, 9, 10);
        int digito2 = calcularDigitoVerificador(cpf, 10, 11);

        return digito1 == digito(cpf, 9) && digito2 == digito(cpf, 10);
    }

    private boolean isValidCNPJ(String cnpj) {
        if (cnpj.length() != TAMANHO_CNPJ || cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int digito1 = calcularDigitoVerificador(cnpj, pesos1);
        int digito2 = calcularDigitoVerificador(cnpj, pesos2);

        return digito1 == digito(cnpj, 12) && digito2 == digito(cnpj, 13);
    }

    private int calcularDigitoVerificador(String numero, int casas, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < casas; i++) {
            soma += digito(numero, i) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }

    private int calcularDigitoVerificador(String numero, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += digito(numero, i) * pesos[i];
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }

    private int digito(String str, int posicao) {
        return str.charAt(posicao) - '0';
    }
}
